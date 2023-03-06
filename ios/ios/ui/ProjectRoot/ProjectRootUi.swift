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
    
    @ObservedObject
    private var test: TestObj = TestObj.init()
    
    init(component: ProjectRoot, closeProject: @escaping () -> Void) {
        self.component = component
        self.routerState = ObservableValue(component.routerState)
        
        NSLog("pre subscribe")
        component.routerState.subscribe { stack in
            NSLog("swift router state update")
        }
        NSLog("post subscribe")
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
        
        Text("project root")
        
        Text(String(test.value) + " test")

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
                        Napier().d(message: "Switch to notes", throwable: nil, tag: "Hammer")
                        test.value = 42
                        component.showNotes()
                    }
                    Button("Time Line") {
                        component.showTimeLine()
                    }
                    Button("Encyclopedia") {
                        component.showEncyclopedia()
                    }
                }
            }
            
            
            let curDest = destinationTitle(dest: activeDestination)
            Text(curDest)

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
                        EmptView.init(dest: dest, title: "Editor")
                    case is Notes:
                        EmptView.init(dest: dest, title: "Notes")
                    case is Encyclopedia:
                        EmptView.init(dest: dest, title: "Encyclopedia")
                    case is TimeLine:
                        EmptView.init(dest: dest, title: "Time Line")
                    case is ProjectHome:
                        EmptView.init(dest: dest, title: "Home")
                    default:
                        //throw KotlinIllegalStateException("Unhandled destination")
                        EmptView.init(dest: dest, title: "unhandled destination")
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

struct EmptView: View {

    private var myDest: ProjectRootDestination<any AnyObject>
    private let title: String
    
    init(dest: ProjectRootDestination<any AnyObject>, title: String) {
        myDest = dest
        self.title = title
    }
    
    var body: some View {
        Text(title)
    }
}
