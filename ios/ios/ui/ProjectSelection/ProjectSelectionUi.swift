//
//  ProjectSelectionUi.swift
//  ios
//
//  Created by Adam Brown on 6/4/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import Hammer

struct ProjectSelectionUi: View {
    
    public init(componentHolder: ComponentHolder<ProjectSelectionComponent>) {
        holder = componentHolder
        state = ObservableValue(componentHolder.component.state)
        
    }
    
    @State
    private var holder: ComponentHolder<ProjectSelectionComponent>
    
    @ObservedObject
    private var state: ObservableValue<ProjectSelectionComponent.State>
    
    @State
    private var directory: String = ""
    
    var body: some View {
        NavigationView {
            VStack() {
                TextField(
                    "Projects Directory",
                    text: $directory
                )
                .onChange(of: directory) { newValue in holder.component.setProjectsDir(path:newValue)
                }
                .textInputAutocapitalization(.never)
                .disableAutocorrection(true)
                .border(.secondary)
                
                Button("Load") {
                    holder.component.loadProjectList()
                }
                
                ScrollView {
                    LazyVStack() {
                        // This isn't working yet, need to subscribe to it some how
                        ForEach(state.value.projects,
                                id: \.self) { value in
                            ProjectItemUi(project: value, onProjectSelected: holder.component.selectProject)
                        }
                    }
                    
                }
            }.navigationTitle("Hammer Projects")
                .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct ProjectSelectionUi_Previews: PreviewProvider {
    static var previews: some View {
        ProjectSelectionUi(
            componentHolder: ComponentHolder { context in
                ProjectSelectionComponent(
                    componentContext: context) { Project in
                        print("Project selected: " + Project.name)
                    }
            }
        )
    }
}

struct ProjectItemUi: View {
    
    private var project: Project
    
    private var onProjectSelected: (Project) -> Void
    
    init(project: Project, onProjectSelected: @escaping (Project) -> Void) {
        self.project = project
        self.onProjectSelected = onProjectSelected
    }
    
    var body: some View {
        Text("Row \(project)")
            .onTapGesture {
                onProjectSelected(project)
            }
    }
}
