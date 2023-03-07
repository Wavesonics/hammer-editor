//
//  ProjectRootUi.swift
//  ios
//
//  Created by Adam Brown on 3/2/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import Hammer

class TestObj: ObservableObject {
    @Published var value = 0
}

struct ProjectRootUi: View {
    
    private let component: ProjectRoot

    @ObservedObject
    private var routerState: ObservableValue<ChildStack<AnyObject, ProjectRootDestination<AnyObject>>>
    private var stack: ChildStack<AnyObject, ProjectRootDestination<AnyObject>> { routerState.value }
    
    private var activeDestination: ProjectRootDestination<AnyObject> { routerState.value.active.instance }
    
    init(component: ProjectRoot, closeProject: @escaping () -> Void) {
        self.component = component
        self.routerState = ObservableValue(component.routerState)
    }
    
    func destinationTitle(dest: ProjectRootDestination<some AnyObject>) -> String {
        switch dest {
        case is ProjectRootDestinationEditorDestination:
            return "Editor"
        case is ProjectRootDestinationNotesDestination:
            return "Notes"
        case is ProjectRootDestinationEncyclopediaDestination:
            return "Encyclopedia"
        case is ProjectRootDestinationTimeLineDestination:
            return "Time Line"
        case is ProjectRootDestinationHomeDestination:
            return "Home"
        default:
            //throw KotlinIllegalStateException("Unhandled destination")
            return "unhandled destination"
        }
    }
    
    var body: some View {
        VStack {
            ScrollView {
                HStack {
                    Button("Home") {
                        component.showHome()
                    }
                    Button("Editor") {
                        component.showEditor()
                    }
                    Button("Notes") {
                        //Napier().d(message: "Switch to notes", throwable: nil, tag: "Hammer")
                        component.showNotes()
                    }
                    Button("Time Line") {
                        component.showTimeLine()
                    }
                    Button("Encyclopedia") {
                        component.showEncyclopedia()
                    }
                }
            }.frame(height: 40)

            StackView(
                stackValue: routerState,
                getTitle: { (dest) -> String in
                    destinationTitle(dest: dest)
                },
                onBack: {}, //stack.active.instance.onBack,
                childContent: {
                    let dest = $0

                    switch dest.component {
                    case is ProjectEditor:
                        Text("Editor")
                    case is Notes:
                        Text("Notes")
                    case is Encyclopedia:
                        Text("Encyclopedia")
                    case is TimeLine:
                        Text("Time Line")
                    case is ProjectHome:
                        Text("Home")
                    default:
                        //throw KotlinIllegalStateException("Unhandled destination")
                        Text("unhandled destination")
                    }
                }
            )
        }
    }
}

//struct ProjectRootUi_Previews: PreviewProvider {
//    static var previews: some View {
//        ProjectRootUi()
//    }
//}
