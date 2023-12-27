//
//  ProjectListUi.swift
//  ios
//
//  Created by Adam Brown on 4/17/23.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import Hammer

// test data, functions
struct TestProject: Identifiable {
    var id: Int
    var Name, Date : String
    private static var lastID = 1
    private static func generateUniqueID() -> Int {
        lastID += 1
        return lastID
    }
    // Initializer to automatically assign a unique ID
    init(name: String, date: String) {
        self.id = Self.generateUniqueID()
        self.Name = name
        self.Date = date
    }
}
func addProject(existingList: inout [TestProject], newStruct: TestProject) {
    existingList.append(newStruct)
}

func delProject(fromList: inout [TestProject], idToDelete: Int) {
    fromList.removeAll { $0.id == idToDelete }
}

// test view of a single project cell
struct ProjectItem : View {
    @State var Project: TestProject
    var onDelete: () -> Void
    
    @State private var isShowingDeleteConfirmation = false
    
    var body: some View {
        HStack {
            VStack {
                NavigationLink(destination: ProjectView(selectedProject: $Project)) {
                    Text(Project.Name)
                        .frame(width: .infinity, alignment: .center)
                        .background(.gray.opacity(0.2))
                        .foregroundColor(Color.black)
                        .cornerRadius(10)
                }
            }
            Button {
                print("delete project clicked")
                isShowingDeleteConfirmation = true
            } label: {
                Label("", systemImage: "trash.fill")
                
            }
            .confirmationDialog("Delete \(Project.Name)?", isPresented: $isShowingDeleteConfirmation, titleVisibility: .visible) {
                Button("Delete", role: .destructive) {
                    print("nuking project...")
                    onDelete()
                }
            }
        }
    }
}

// create project dialog
struct CreateDialog : View {
    @Binding var visible: Bool
    @Binding var projects: [TestProject]
    
    @State private var newProjectName = ""
    
    var body: some View {
        VStack {
            Text("Create Project")
                .font(.title2)
                .bold()
            // TODO: styling
            TextField("New Project Name", text: $newProjectName)
                .background(Color.gray.opacity(0.1).cornerRadius(3))
                .keyboardType(.asciiCapableNumberPad)
            Button {
                if isValidName() {
                    addProject(existingList: &projects, newStruct: TestProject(name: newProjectName, date: "7/17/24"))
                    print("addproject called")
                    visible = false
                }
            } label: {
                ZStack {
                    RoundedRectangle(cornerRadius: 10)
                        .foregroundColor(.blue)
                    Text("create project")
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(.white)
                        .padding()
                }
                .padding()
            }
            .disabled(!isValidName())
        }
        .fixedSize(horizontal: false, vertical: true)
        .padding()
        .background(.white)
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .shadow(radius: 20)
        .padding()
        .overlay {
            VStack {
                HStack {
                    Spacer()
                    Button {
                        visible = false
                    } label: {
                        Label("", systemImage: "xmark") // TODO: figure out how to hide that label
                    }
                    .tint(.black)
                }
                Spacer()
            }
            .padding()
        }
        
    } //view
    func isValidName() -> Bool {
        if newProjectName.count < 1 {
            return false
        }
        return true
    }
}

// view of the front page of a project (the second page in the main nav)
struct ProjectView : View {
    @Binding var selectedProject: TestProject
    
    @State private var selectedTab = ""
    var body: some View {
        VStack {
        }
        .navigationTitle(selectedTab == "Stats" ? selectedProject.Name: selectedTab )  // TODO: speed this up
        .toolbar {
            // TODO: not sure if we wanna do this if we have a create button in the scrollview?
            if selectedTab == "Notes" {
                ToolbarItemGroup {
                    Button {
                        print("create note button tapped")
                    } label: {
                        Label("create", systemImage: "pencil")
                    }
                }
            }
            if selectedTab == "Stats" {
                ToolbarItemGroup {
                    Button {
                        print("create note button tapped")
                    } label: {
                        Label("create", systemImage: "square.and.arrow.up")
                    }
                }
            }
        }
        TabView {
            StatsView() //temporary while debugging
                .onAppear(){
                    selectedTab = "Stats"
                }
                .tabItem {
                    Image(systemName: "house")
                    Text("Stats")
                }
            ScenesProjectView()
                .onAppear(){
                    selectedTab = "Scenes"
                }
                .tabItem {
                    Image(systemName: "pencil")
                    Text("Scenes")
                }
            NotesView()
                .onAppear(){
                    selectedTab = "Notes"
                }
                .tabItem {
                    Image(systemName: "note.text")
                    Text("Notes")
                    
                }
                .navigationTitle("Notes")
            DictionaryView()
                .onAppear(){
                    selectedTab = "Dictionary"
                }
                .tabItem {
                    Image(systemName: "book")
                    Text("dictionary")
                }
            TimelineView()
                .onAppear(){
                    selectedTab = "Timeline"
                }
                .tabItem {
                    Image(systemName: "calendar")
                    Text("timeline")
                }
        }
        
    }
}

struct StatsView : View {
    var body: some View {
        ScrollView {
            VStack {
                ZStack {
                    RoundedRectangle(cornerRadius: 15)
                        .foregroundColor(.gray.opacity(0.2))
                    VStack(alignment: .center) {
                        Text("99")
                            .font(.largeTitle)
                            .foregroundColor(.black)
                            .frame(width: .infinity, alignment: .center)
                        Text("Scenes")
                            .foregroundColor(.black)
                    }
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding()
                }
                .padding()
                
                ZStack {
                    RoundedRectangle(cornerRadius: 15)
                        .foregroundColor(.gray.opacity(0.2))
                    VStack(alignment: .center) {
                        Text("33,191")
                            .font(.largeTitle)
                            .foregroundColor(.black)
                            .frame(width: .infinity, alignment: .center)
                        Text("Words")
                            .foregroundColor(.black)
                    }
                    .frame(maxWidth: .infinity, alignment: .center)
                    .padding()
                }
                .padding()
                
                ZStack {
                    RoundedRectangle(cornerRadius: 15)
                        .foregroundColor(.gray.opacity(0.2))
                    Text("bar graph")
                        .frame(maxWidth: .infinity, alignment: .center)
                        .padding()
                }
                .padding()
                
            }
        }
    }
}
struct ScenesProjectView : View {
    var body: some View {
        Text("editing scenes")
    }
}

struct NoteEditView : View {
    @State private var isShowingDeleteConfirmation = false
    @State private var textBody = "what the heck"
    var body: some View {
        VStack {
            TextField("edit a note here", text: $textBody)
                .navigationTitle("Note")
                .border(.black)
        }
        .toolbar {
            ToolbarItemGroup(placement: .navigationBarTrailing) {
                Button {
                    // open new "are you sure?" at bottom of screen
                    isShowingDeleteConfirmation = true
                } label: {
                    Label("Delete This Note", systemImage: "trash")
                }
                .confirmationDialog("Delete Note?", isPresented: $isShowingDeleteConfirmation, titleVisibility: .visible) {
                    Button("Delete", role: .destructive) {
                        print("nuking note...")
                        //                        onDelete()
                    }
                }
            }
        }
    }
}

struct NotesView : View {
    @State private var noteOneText = "This is a longer sentence showing what a longer line looks like on the screen. It should wrap to the next line."
    var body: some View {
        
        ScrollView {
            NavigationLink(destination: NoteEditView()) {
                Text("create new note")
            }
            
            VStack(alignment: .leading) {
                ZStack {
                    RoundedRectangle(cornerRadius: 10)
                        .foregroundColor(.gray.opacity(0.2))
                    
                    NavigationLink(destination: NoteEditView()) {
                        VStack(alignment: .leading) {
                            Text("note title")
                                .font(.largeTitle)
                                .foregroundColor(.black)
                                .frame(width: .infinity)
                            Text(noteOneText)
                                .multilineTextAlignment(.leading)
                                .foregroundColor(.black)
                            Spacer()
                            Text("(the date)")
                                .foregroundColor(.black)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .border(.green)
                        .padding()
                    }
                    
                } //zstack1
                .padding()
                
                ZStack {
                    RoundedRectangle(cornerRadius: 10)
                        .foregroundColor(.gray.opacity(0.2))
                    
                    NavigationLink(destination: NoteEditView()) {
                        VStack(alignment: .leading) {
                            Text("another note title")
                                .font(.largeTitle)
                                .foregroundColor(.black)
                                .frame(width: .infinity)
                            Text("this is a shorter note")
                                .multilineTextAlignment(.leading)
                                .foregroundColor(.black)
                            Spacer()
                            Text("(the date)")
                                .foregroundColor(.black)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .border(.green)
                        .padding()
                    }
                    
                } //zstack1
                .padding()
            }//vstack
            
        } //scrollview
        
        //        NavigationLink(destination: NoteEditView()) {
        //            Text("create new note")
        //        }
    }
}
struct DictionaryView : View {
    @State private var searchTerm = ""

    let events: [Event] = [
        Event(title: "that one thing", date: "2023-01-01"),
        Event(title: "another thing which happened", date: "2023-02-01"),
        Event(title: "otters and other animals", date: "2023-03-01"),
    ]
    
    var filteredEvents: [Event] {
        guard !searchTerm.isEmpty else { return events }
        return events.filter { $0.title.localizedCaseInsensitiveContains(searchTerm) }
    }
    
    var body: some View {
        NavigationView {
            List(filteredEvents, id:\.title) { event in
                Text(event.title)
            }
            .searchable(text: $searchTerm, placement: .automatic, prompt: "search by name or hashtag")
        }
    }
}

struct Event: Identifiable {
    var id = UUID()
    var title: String
    var date: String
}

// vertical and horizontal bars for the vertical timeline
struct TimeLink: Shape {
    var horizontal = true
    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.move(to: CGPoint(x: rect.midX, y: rect.maxY))
        path.addLine(to: CGPoint(x: rect.midX, y: rect.minY))
        if horizontal {
            path.move(to: CGPoint(x: rect.midX, y: rect.midY))
            path.addLine(to: CGPoint(x: rect.maxX, y: rect.midY))
        }
        return path
    }
}

struct TimelineView: View {
    let events: [Event] = [
        Event(title: "Event 1", date: "2023-01-01"),
        Event(title: "Event 2", date: "2023-02-01"),
        Event(title: "Event 3", date: "2023-03-01"),
    ]
    
    var body: some View {
        ScrollView {
            VStack(spacing: 0) {
                ForEach(events) { event in
                    VStack(spacing: 0) {
                        HStack(spacing: 0) {
                            // the date hstack for this cell
                            TimeLink(horizontal: false)
                                .stroke(Color.black)
                                .frame(width: 40)
                            // bumping the padding of this bumps the left side line/shape too
                            Text(event.date)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .fontWeight(.medium)
                                .padding(.vertical)
                        }
                        HStack {
                            // the content hstack for this cell
                            TimeLink()
                                .stroke(Color.black)
                                .frame(width: 40)
                            //                            .border(.red)
                            ZStack {
                                RoundedRectangle(cornerRadius: 10)
                                    .foregroundColor(.gray.opacity(0.2))
                                
                                VStack(alignment: .leading) {
                                    
                                    Text("event text")
                                        .multilineTextAlignment(.leading)
                                        .foregroundColor(.black)
                                    
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding()
                                
                            } //zstack1
                        }
                    }
                }
            }
            .padding()
        }
    }
}

struct TimelineEventView: View {
    var event: Event
    
    var body: some View {
        HStack(alignment: .top, spacing: 20) {
            // Timeline Dot
            Circle()
                .frame(width: 20, height: 20)
                .foregroundColor(Color.blue)
            
            // Vertical Line
            GeometryReader { geometry in
                Path { path in
                    let center = CGPoint(x: geometry.frame(in: .local).midX, y: geometry.frame(in: .local).maxY)
                    path.move(to: center)
                    path.addLine(to: CGPoint(x: center.x, y: center.y + 30))
                }
                .stroke(Color.gray, lineWidth: 1)
            }
            .frame(width: 20, height: 40)
            
            // Event Details
            VStack(alignment: .leading, spacing: 4) {
                Text(event.title)
                    .font(.headline)
                Text(event.date)
                    .font(.subheadline)
                    .foregroundColor(Color.gray)
            }
            
            // Horizontal Line
            Spacer()
            Path { path in
                path.move(to: CGPoint(x: 0, y: 10))
                path.addLine(to: CGPoint(x: 10, y: 10))
            }
            .stroke(Color.gray, lineWidth: 1)
        }
    }
}


// end of test code


// the real code
struct ProjectsListUi: View {
    // some initial data, less button clicking
    @State private var projects: [TestProject] = [
        .init(name: "Project 1", date: "12/25/23"),
        .init(name: "Project 2", date: "12/31/23"),
        .init(name: "Project 3", date: "1/5/24")
    ]
    @State private var isShowingCreateDialog = false
    //    @State private var selectedProject = 1
    //    private let component: ProjectsList
    
    //    @ObservedObject
    //    private var observableState: ObservableValue<ProjectsListState>
    
    //    private var state: ProjectsListState { observableState.value }
    
    //    init(component: ProjectsList) {
    //        self.component = component
    //        self.observableState = ObservableValue(component.state)
    //    }
    
    var body: some View {
        NavigationView {
            ZStack {
                VStack() {
                    // vertical scroll view of existing projects
                    ScrollView {
                        // Display a list of structs in a ScrollView
                        VStack {
                            ForEach(projects) { item in
                                ProjectItem(Project: item) {
                                    // Closure to handle the delete action
                                    if let index = projects.firstIndex(where: { $0.id == item.id }) {
                                        projects.remove(at: index)
                                    }
                                }
                            }
                        }
                    }
                    .padding()
                    .toolbar {
                        Button() {
                            isShowingCreateDialog = true
                            print("create dialog opening")
                        } label: {
                            Label("Create Project", systemImage: "doc.badge.plus")
                        }
                    }
                } // vstack
                .frame(maxWidth: 300, alignment: Alignment.center)
                .padding()
                .navigationBarTitle("Projects", displayMode: .large)
                if isShowingCreateDialog {
                    CreateDialog(visible: $isShowingCreateDialog, projects: $projects)
                        .frame(alignment: Alignment.center)
                }
            } //zstack
        } //nav
    } //view
}

struct ProjectListUi_Previews: PreviewProvider {
    static var previews: some View {
        ProjectsListUi()
        //ProjectsListUi(component: ProjectsList)
    }
}
