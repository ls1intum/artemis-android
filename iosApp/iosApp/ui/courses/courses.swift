import Foundation
import SwiftUI
import SDWebImageSwiftUI

struct CoursesHeaderView<Content: View>: View {

    let course: Course
    let bearer: String
    let contentView: Content

    let courseIconUrl: String?

    init(course: Course, serverUrl: String, bearer: String, @ViewBuilder content: () -> Content) {
        self.course = course
        self.bearer = bearer

        if (course.courseIcon != nil) {
            courseIconUrl = serverUrl.dropLast() + (course.courseIcon ?? "")
        } else {
            courseIconUrl = nil
        }

        contentView = content()
    }

    var body: some View {
        let cardShape = RoundedRectangle(cornerRadius: 10, style: .continuous)
        return ZStack {
            VStack {
                HStack(alignment: .top) {
                    if courseIconUrl != nil {
                        WebImage(
                                url: URL(string: courseIconUrl!),
                                context: [.downloadRequestModifier: SDWebImageDownloaderRequestModifier(headers: ["Authorization": bearer])]
                        )
                                .placeholder {
                                    Image(systemName: "questionmark")
                                            .resizable()
                                            .scaledToFit()
                                            .padding(.all, 8)
                                }
                                .resizable()
                                .aspectRatio(1, contentMode: .fill)
                                .frame(width: 80, height: 80, alignment: .center)
                                .scaledToFit()
                    } else {
                        ZStack {
                            Image(systemName: "questionmark")
                                    .resizable()
                                    .scaledToFit()
                                    .padding(.all, 8)
                        }
                                .frame(width: 80, height: 80, alignment: .center)
                    }

                    VStack(alignment: .leading) {
                        Text(course.title ?? "")
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .lineLimit(2)
                                .minimumScaleFactor(0.8)
                                .font(.title2)

                        Text(course.description ?? "")
                                .foregroundColor(Color.primaryContainer.onSurface)
                                .lineLimit(3)
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
                .clipShape(cardShape)
                .background(
                        cardShape
                                .stroke(Color.outline)
                )
                .background(
                        cardShape
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

            CoursesHeaderView(
                    course: Course(id: 12, title: "Introduction to CS. Introduction to CS. Introduction to CS. Introduction to CS.", description: "Learn how to apply software engineering skills. Learn how to apply software engineering skills.", courseIcon: nil),
                    serverUrl: "https://placeholder.com/",
                    bearer: ""
            ) {

            }
                    .padding(.horizontal, 8)
        }
    }
}
