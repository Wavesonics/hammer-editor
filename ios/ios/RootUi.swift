//
//  RootUi.swift
//  ios
//
//  Created by Adam Brown on 6/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import Hammer

struct RootUi: View {
    private let root: RootHolder

    @ObservedObject
    private var observableState: ObservableValue<RootState>

    private var state: RootState { observableState.value }

    init(_ root: RootHolder) {
        self.root = root
        observableState = ObservableValue(root.state)
    }

    var body: some View {
//        let path = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0].appendingPathComponent("todos.txt")
//
//        let todos = "sabotage Adam"
//
//        do {
//        try todos.write(to: path, atomically: true, encoding: .utf8)
//        } catch {
//        print(error.localizedDescription)
//        }
        
//        let path = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask)[0].appendingPathComponent("testFolder")
//
//        if !FileManager.default.fileExists(atPath: path.absoluteString) {
//        try! FileManager.default.createDirectory(at: path, withIntermediateDirectories: true, attributes: nil)
//        }
//        return Text("duh")
        // TODO, commented out while we test I/O issues
        if(state.projectSelected == nil) {
            createProjectSelect(root: root)
        }
//        else {
//            createProjectEditor(project: state.projectSelected!, root: root)
//        }
        
    }
}
//
private func createProjectSelect(root: RootHolder) -> ProjectSelectionUi {
    let projectSelectionHolder = ComponentHolder<ProjectSelectionComponent> { context in
        ProjectSelectionComponent(
            componentContext: context, showProjectDirectory: false) { project in
                print("Project selected: " + project.name)
                root.selectProject(project: project)
            }
    }

    // Create the SwiftUI view that provides the window contents.
    return ProjectSelectionUi(componentHolder: projectSelectionHolder)
}
//
//private func createProjectEditor(project: ProjectDefinition, root: RootHolder) -> ProjectEditorUi {
//    let component = ComponentHolder<ProjectEditorComponent> { context in
//        ProjectEditorComponent(
//            componentContext: context,
//            projectDef: project,
//            addMenu: { menu in
//                NSLog("Add menu item")
//            },
//            removeMenu: { menuItemId in
//                NSLog("Remove menu item")
//            }
//        )
//    }
//
//    let projectEditorView = ProjectEditorUi(componentHolder: component) {
//        root.closeProject()
//    }
//
//    return projectEditorView
//}
