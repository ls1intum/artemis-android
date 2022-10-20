import Foundation
import SwiftUI
import SDWebImageSwiftUI

struct CoursesHeaderView<Content: View>: View {

    let course: Course
    let bearer: String
    let contentView: Content

    let courseIconUrl: String

    init(course: Course, serverUrl: String, bearer: String, @ViewBuilder content: () -> Content) {
        self.course = course
        self.bearer = bearer

        courseIconUrl = serverUrl.dropLast() + course.courseIcon

        contentView = content()
    }

    var body: some View {
        ZStack {
            VStack {
                HStack(alignment: .top) {
                    WebImage(
                            url: URL(string: courseIconUrl),
                            context: [.downloadRequestModifier: SDWebImageDownloaderRequestModifier(headers: ["Authorization": bearer])]
                    )
                            .placeholder(Image(systemName: "photo"))
                            .resizable()
                            .aspectRatio(1, contentMode: .fill)
                            .frame(width: 80, height: 80, alignment: .center)
                            .scaledToFit()

                    VStack(alignment: .leading) {
                        Text(course.title)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .lineLimit(2)
                                .minimumScaleFactor(0.8)
                                .font(.title2)

                        Text(course.description)
                                .foregroundColor(Color.primaryContainer.onSurface)
                                .font(.caption)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .multilineTextAlignment(.leading)
                                .fixedSize(horizontal: false, vertical: true)
                    }
                            .frame(maxWidth: .infinity)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 8)
                }

                contentView
            }
        }
                .background(
                        RoundedRectangle(cornerRadius: 10, style: .continuous)
                                .stroke(Color.outline)
                )
                .background(
                        RoundedRectangle(cornerRadius: 10, style: .continuous)
                                .fill(Color.primaryContainer.surface)
                )
    }
}

struct CoursesHeaderViewPreviews: PreviewProvider {
    static var previews: some View {
        Group {
            CoursesHeaderView(
                    course: Course(id: 12, title: "Introduction to CS. Introduction to CS. Introduction to CS. Introduction to CS.", description: "Learn how to apply software engineering skills. Learn how to apply software engineering skills.", courseIcon: "150"),
                    serverUrl: "https://placeholder.com/",
                    bearer: ""
            ) {

            }
                    .padding(.horizontal, 8)
        }
    }
}