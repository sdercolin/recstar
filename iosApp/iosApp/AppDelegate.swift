import Foundation
import UIKit
import shared

class AppDelegate: NSObject, UIApplicationDelegate {

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
        let appRootPath = self.getAppRootPath()
        let contentRootPath = self.getContentRootPath()
        Paths().initializeAppRootPath(path: appRootPath)
        Paths().initializeContentRootPath(path: contentRootPath)
        PathsKt.ensurePaths()
        Log().initialize()

        return true
    }

    func getContentRootPath() -> String {
        // Get the shared documents directory path
        guard let contentFolderPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first?.path else {
            fatalError("Unable to get documents directory.")
        }
        
        // Check if directory exists. If not, create it
        let fileManager = FileManager.default
        if !fileManager.fileExists(atPath: contentFolderPath) {
            do {
                try fileManager.createDirectory(atPath: contentFolderPath, withIntermediateDirectories: true, attributes: nil)
            } catch {
                print("Error creating content directory: \(error)")
            }
        }
        
        return contentFolderPath
    }

    func getAppRootPath() -> String {
        // Get the application support directory path
        guard let appFolderPath = FileManager.default.urls(for: .applicationSupportDirectory, in: .userDomainMask).first?.path else {
            fatalError("Unable to get application support directory.")
        }

        // Check if directory exists. If not, create it
        let fileManager = FileManager.default
        if !fileManager.fileExists(atPath: appFolderPath) {
            do {
                try fileManager.createDirectory(atPath: appFolderPath, withIntermediateDirectories: true, attributes: nil)
            } catch {
                print("Error creating app directory: \(error)")
            }
        }

        return appFolderPath
    }

    func application(_ application: UIApplication, supportedInterfaceOrientationsFor window: UIWindow?) -> UIInterfaceOrientationMask {
        let orientation = ProvideScreenOrientationKt.getRequestedOrientation()
        switch orientation {
            case 0:
                return .portrait
            case 1:
                return .landscape
            default:
                return .all
        }
    }
}
