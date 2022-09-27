//
//  CoursesOverviewView.swift
//  iosApp
//
//  Created by Tim Ortel on 16.09.22.
//
//  Display the course overview with the course list.
//

import SwiftUI
import Combine
import SDWebImageSwiftUI
import SDWebImage

struct CoursesOverviewView: View {

    @StateObject var viewModel: CoursesOverviewViewModel = CoursesOverviewViewModel()

    var body: some View {
        NavigationView {
            VStack(alignment: .center) {

                switch (viewModel.dashboard) {
                case .loading: ProgressView()
                        .progressViewStyle(CircularProgressViewStyle())
                case .done(let response): DashboardLoadedView(response: response, serverUrl: viewModel.serverCommunicationProvider.serverUrl, bearer: viewModel.bearer)
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                }
            }
                    .navigationTitle(Text("Course Overview"))
                    .navigationBarItems(trailing: Button("Logout") {
                        viewModel.logout()
                    })
                    .task {
                        await viewModel.reloadDashboard()
                    }
        }
    }
}

private struct DashboardLoadedView: View {

    let response: NetworkResponse<Dashboard>
    let serverUrl: String
    let bearer: String

    var body: some View {
        VStack {
            switch (response) {
            case .response(let data): VStack {
                let courses: [Course] = data.courses

                CoursesList(courses: courses, serverUrl: serverUrl, bearer: bearer)
            }
            case .failure( _): VStack {
                Text("Failed loading dashboard.")
            }
            default: EmptyView()
            }
        }
    }
}

private struct CoursesList: View {
    let courses: [Course]
    let serverUrl: String
    let bearer: String

    var body: some View {
        ScrollView {
            VStack {
                ForEach(courses, id: \.self.id) { course in
                    CourseView(course: course, serverUrl: serverUrl, bearer: bearer)

                    if course.id != courses.last?.id {
                        Divider()
                                .frame(maxWidth: .infinity)
                    }
                }

                Spacer()
            }
        }
                .padding(.leading, 25)
                .padding(.trailing, 25)
    }
}

private struct CourseView: View {
    let course: Course
    let serverUrl: String
    let bearer: String

    var body: some View {
        let url: String = serverUrl.dropLast() + course.courseIcon

        HStack {
            WebImage(
                    url: URL(string: url),
                    context: [.downloadRequestModifier: SDWebImageDownloaderRequestModifier(headers: ["Authorization": bearer])]
            )
                    .placeholder(Image(systemName: "photo"))
                    .resizable()
                    .aspectRatio(1, contentMode: .fill)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .scaledToFit()
                    .clipShape(Circle())

            VStack(alignment: .leading, spacing: 4) {
                Text(course.title)
                        .font(.system(size: 30, weight: .bold))
                        .frame(maxWidth: .infinity, alignment: .leading)

                Text(course.description)
                        .frame(maxWidth: .infinity, alignment: .leading)
            }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
                .frame(maxHeight: 80)
    }
}


class CoursesOverviewView_Previews: PreviewProvider {
    static var previews: some View {
        Group {
//            let serverUrl = "https://via.placeholder.com"

//            let sampleCourse = Course(id: 12, title: "Sample Course", description: "Sample Course Description", courseIconPath: "/150/0000FF")
//
//            let courses = [sampleCourse,
//                           Course(id: 13,
//                                   title: "Other Course", description: "Playing with penguins", courseIconPath: "/150/0000FF"),
//                           Course(id: 14,
//                                   title: "Another Course", description: "Description 123", courseIconPath: "/150/0000FF"),
//            ]
//
//            CoursesList(courses: courses, serverUrl: "", bearer: "")
//
//            CourseView(course: sampleCourse, serverUrl: serverUrl, bearer: "")
        }
    }
}
