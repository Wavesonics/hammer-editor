package com.darkrockstudios.apps.hammer

import com.darkrockstudios.apps.hammer.base.http.readToml
import com.darkrockstudios.apps.hammer.plugins.*
import com.darkrockstudios.apps.hammer.plugins.kweb.configureKweb
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import net.peanuuutz.tomlkt.Toml
import okio.FileSystem
import okio.Path.Companion.toPath
import java.io.File
import java.security.KeyStore

fun main(args: Array<String>) {
	val parser = ArgParser("Hammer Server")
	val configPathArg by parser.option(
		ArgType.String,
		shortName = "c",
		fullName = "config",
		description = "Server Config Path"
	)
	val devModeArg by parser.option(
		ArgType.Boolean,
		shortName = "d",
		fullName = "dev",
		description = "Run in development mode"
	)

	parser.parse(args)

	val config: ServerConfig = configPathArg?.let {
		loadConfig(it)
	} ?: ServerConfig()

	startServer(config, devModeArg ?: false)
}

private fun loadConfig(path: String): ServerConfig {
	return FileSystem.SYSTEM.readToml(path.toPath(), Toml, ServerConfig::class)
}

private fun startServer(config: ServerConfig, devMode: Boolean) {
	// This is overkill most of the time
	//	if(devMode) {
	//		// Sets the log mode for SLFJ, if we ever move to Logback, we'll need to set this a different way
	//		System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "DEBUG");
	//	}

	val environment = applicationEngineEnvironment {
		val bindHost = "0.0.0.0"
		connector {
			port = config.port
			host = bindHost
		}

		config.sslCert?.apply {
			sslConnector(
				keyStore = getKeyStore(this),
				keyAlias = keyAlias ?: "",
				keyStorePassword = { storePassword.toCharArray() },
				privateKeyPassword = { (keyPassword ?: "").toCharArray() }) {
				keyStorePath = File(path)
				host = bindHost
				port = config.sslPort
			}
		}

		module {
			appMain(config)
		}

		developmentMode = devMode
		if (devMode) {
			watchPaths += listOf("classes")
		}
	}

	embeddedServer(
		Jetty,
		environment = environment
	).start(wait = true)
}

private fun getKeyStore(sslConfig: SslCertConfig): KeyStore {
	val certFile = File(sslConfig.path)
	if (certFile.exists().not()) throw IllegalArgumentException("SSL Cert not found")
	return KeyStore.getInstance(certFile, sslConfig.storePassword.toCharArray())
}

fun Application.appMain(config: ServerConfig) {
	configureDependencyInjection()
	configureSerialization()
	configureMonitoring()
	configureHTTP(config)
	configureSecurity()
	configureLocalization()
	configureRouting()
	configureKweb(config)
}
