package com.darkrockstudios.apps.hammer.common.projectselection

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.extensions.compose.jetbrains.subscribeAsState
import com.darkrockstudios.apps.hammer.MR
import com.darkrockstudios.apps.hammer.common.compose.ExposedDropDown
import com.darkrockstudios.apps.hammer.common.compose.MpDialog
import com.darkrockstudios.apps.hammer.common.compose.Ui
import com.darkrockstudios.apps.hammer.common.compose.moko.get
import com.darkrockstudios.apps.hammer.common.globalsettings.UiTheme
import com.darkrockstudios.libraries.mpfilepicker.DirectoryPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun Settings(
    component: ProjectSelection,
    modifier: Modifier = Modifier
) {
    val state by component.state.subscribeAsState()

    var projectsPathText by remember { mutableStateOf(state.projectsDir.path) }
    var showDirectoryPicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .padding(Ui.Padding.L)
                .fillMaxSize()
        ) {
            Text(
                MR.strings.settings_heading.get(),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(Ui.Padding.L)
            )

            Divider(modifier = Modifier.fillMaxWidth())

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.size(Ui.Padding.L))
                Column(modifier = Modifier.padding(Ui.Padding.M)) {
                    Text(
                        MR.strings.settings_theme_label.get(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    Spacer(modifier = Modifier.size(Ui.Padding.M))

                    val themeOptions = remember { UiTheme.values().toList() }
                    ExposedDropDown(
                        modifier = Modifier.defaultMinSize(minWidth = 256.dp),
                        items = themeOptions,
                        defaultIndex = themeOptions.indexOf(state.uiTheme)
                    ) { selectedTheme ->
                        if (selectedTheme != null) {
                            component.setUiTheme(selectedTheme)
                        }
                    }
                }

                if (component.showProjectDirectory) {
                    Spacer(modifier = Modifier.size(Ui.Padding.XL))

                    Column(modifier = Modifier.padding(Ui.Padding.M)) {
                        Text(
                            MR.strings.settings_projects_directory.get(),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                        )

                        Spacer(modifier = Modifier.size(Ui.Padding.M))

                        TextField(
                            value = projectsPathText,
                            onValueChange = { projectsPathText = it },
                            enabled = false,
                            label = {
                                Text(MR.strings.settings_projects_directory_hint.get())
                            }
                        )

                        Spacer(modifier = Modifier.size(Ui.Padding.M))

                        Button(onClick = { showDirectoryPicker = true }) {
                            Text(MR.strings.settings_projects_directory_button.get())
                        }
                    }
                }

                Spacer(modifier = Modifier.size(Ui.Padding.XL))

                Column(modifier = Modifier.padding(Ui.Padding.M)) {
                    Text(
                        MR.strings.settings_example_project_header.get(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        MR.strings.settings_example_project_description.get(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontStyle = FontStyle.Italic
                    )

                    Spacer(modifier = Modifier.size(Ui.Padding.M))

                    val successMessage = MR.strings.settings_example_project_success_message.get()
                    Button(onClick = {
                        scope.launch {
                            component.reinstallExampleProject()
                            snackbarHostState.showSnackbar(successMessage)
                        }
                    }) {
                        Text(MR.strings.settings_example_project_button.get())
                    }
                }

                Spacer(modifier = Modifier.size(Ui.Padding.XL))

                Column(modifier = Modifier.padding(Ui.Padding.M)) {
                    Text(
                        MR.strings.settings_server_header.get(),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Text(
                        MR.strings.settings_server_description.get(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontStyle = FontStyle.Italic
                    )

                    val serverUrl = state.serverUrl
                    if (serverUrl == null) {
                        Button(onClick = {
                            scope.launch {
                                component.beginSetupServer()
                            }
                        }) {
                            Text(MR.strings.settings_server_setup_button.get())
                        }
                    } else {
                        Row {
                            Text(
                                MR.strings.settings_server_url_label.get(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground,
                            )

                            Text(
                                serverUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontStyle = FontStyle.Italic
                            )

                            Button(onClick = {
                                scope.launch {
                                    //component.beginSetupServer()
                                }
                            }) {
                                Text(MR.strings.settings_server_modify_button.get())
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    ServerSetup(state, component, scope, snackbarHostState)

    DirectoryPicker(showDirectoryPicker) { path ->
        showDirectoryPicker = false

        if (path != null) {
            projectsPathText = path
            component.setProjectsDir(projectsPathText)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServerSetup(
    state: ProjectSelection.State,
    component: ProjectSelection,
    scope: CoroutineScope,
    snackbarHostState: SnackbarHostState
) {
    MpDialog(
        onCloseRequest = { component.cancelServerSetup() },
        visible = state.serverSetup,
        title = MR.strings.settings_server_setup_title.get(),
    ) {
        var urlValue by rememberSaveable { mutableStateOf("") }
        var emailValue by rememberSaveable { mutableStateOf("") }
        var passwordValue by rememberSaveable { mutableStateOf("") }
        var passwordVisible by rememberSaveable { mutableStateOf(false) }

        Column {
            OutlinedTextField(
                value = urlValue,
                onValueChange = { urlValue = it },
                label = { Text(MR.strings.settings_server_setup_url_hint.get()) }
            )

            OutlinedTextField(
                value = emailValue,
                onValueChange = { emailValue = it },
                label = { Text(MR.strings.settings_server_setup_email_hint.get()) }
            )

            OutlinedTextField(
                value = passwordValue,
                onValueChange = { passwordValue = it },
                label = { Text(MR.strings.settings_server_setup_password_hint.get()) },
                singleLine = true,
                placeholder = { Text(MR.strings.settings_server_setup_password_hint.get()) },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible)
                        Icons.Filled.Visibility
                    else Icons.Filled.VisibilityOff

                    // Please provide localized description for accessibility services
                    val description = if (passwordVisible) "Hide password" else "Show password"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, description)
                    }
                }
            )

            Row {
                Button(onClick = {
                    scope.launch {
                        if (component.setupServer(
                                url = urlValue,
                                email = emailValue,
                                password = passwordValue,
                                create = false
                            )
                        ) {
                            snackbarHostState.showSnackbar("Server setup complete!")
                        } else {
                            snackbarHostState.showSnackbar("Failed to setup server")
                        }
                    }
                }) {
                    Text(MR.strings.settings_server_setup_login_button.get())
                }

                Button(onClick = {
                    scope.launch {
                        if (component.setupServer(
                                url = urlValue,
                                email = emailValue,
                                password = passwordValue,
                                create = true
                            )
                        ) {
                            snackbarHostState.showSnackbar("Server setup complete!")
                        } else {
                            snackbarHostState.showSnackbar("Failed to setup server")
                        }
                    }
                }) {
                    Text(MR.strings.settings_server_setup_create_button.get())
                }

                Button(onClick = {
                    scope.launch {
                        component.cancelServerSetup()
                    }
                }) {
                    Text(MR.strings.settings_server_setup_cancel_button.get())
                }
            }
        }
    }
}