import SwiftUI
import appCommon

@main
struct iOSApp: App {
    
    @StateObject
    private var rootComponentHolder = RootComponentHolder()
    
    init() {
        KoinHelperKt.doInitKoin()
    }
    
	var body: some Scene {
		WindowGroup {
            RootView(rootComponentHolder.root)
                .onAppear { LifecycleRegistryExtKt.resume(self.rootComponentHolder.lifecycle) }
                .onDisappear { LifecycleRegistryExtKt.stop(self.rootComponentHolder.lifecycle) }
		}
	}
}

private class RootComponentHolder : ObservableObject {
    let lifecycle: LifecycleRegistry
    let root: RootComponent
    
    init() {
        lifecycle = LifecycleRegistryKt.LifecycleRegistry()
            
        root = RootComponent(
            componentContext: DefaultComponentContext(lifecycle: lifecycle)
        )
            
        lifecycle.onCreate()
    }
        
    deinit {
        lifecycle.onDestroy()
    }
}
