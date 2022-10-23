import Foundation
import SwiftUI

/**
 * Displays ui for each data state.
 * For loading, a progress bar with text on top of it
 * For suspended and failure, text with a try again button.
 */
struct BasicDataStateView<T, ChildContent: View>: View {

    let data: DataState<T>
    let successUi: ChildContent?
    let loadingText: LocalizedStringKey
    let failureText: LocalizedStringKey
    let suspendedText: LocalizedStringKey
    let retryButtonText: LocalizedStringKey
    let clickRetryButtonAction: () -> Void

    init(
            data: DataState<T>,
            loadingText: LocalizedStringKey,
            failureText: LocalizedStringKey,
            suspendedText: LocalizedStringKey,
            retryButtonText: LocalizedStringKey,
            clickRetryButtonAction: @escaping () -> Void,
            @ViewBuilder successUi: (T) -> ChildContent
    ) {
        self.data = data
        self.loadingText = loadingText
        self.failureText = failureText
        self.suspendedText = suspendedText
        self.retryButtonText = retryButtonText
        self.clickRetryButtonAction = clickRetryButtonAction

        switch data {
        case .done(
                let data
        ): self.successUi = successUi(data)
        default: self.successUi = nil
        }

    }

    var body: some View {
        ZStack {
            switch data {
            case .failure, .suspended:
                VStack {
                    let text: LocalizedStringKey = {
                        switch data {
                        case .suspended(error: _): return suspendedText
                        case .failure(error: _): return failureText
                        default: return ""
                        }
                    }()

                    Text(text)

                    Button(action: clickRetryButtonAction) {
                        Text(retryButtonText)
                    }
                }
                        .frame(alignment: Alignment.center)
            case .loading:
                VStack {
                    Text(loadingText)

                    ProgressView()
                }
                        .frame(alignment: Alignment.center)
            case .done(_):
                successUi!
            }
        }
    }
}

class DataStateViewPreviews: PreviewProvider {
    static var previews: some View {
        Group {
            BasicDataStateView(
                    data: DataState.suspended(error: nil),
                    loadingText: "Loading data",
                    failureText: "Failed loading data",
                    suspendedText: "Unstable internet connection",
                    retryButtonText: "Try again",
                    clickRetryButtonAction: {}
            ) {
            }

            BasicDataStateView(
                    data: DataState.loading,
                    loadingText: "Loading data",
                    failureText: "Failed loading data",
                    suspendedText: "Unstable internet connection",
                    retryButtonText: "Try again",
                    clickRetryButtonAction: {}
            ) {
            }
        }
    }
}