import Foundation
import UIKit
import shared

class AppDelegate: NSObject, UIApplicationDelegate {
    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {

        let appRootPath = self.getAppRootPath()

        // Call the Kotlin function to initialize the path.
        Paths().initializeAppRootPath(path: appRootPath)

        return true
    }

    func getAppRootPath() -> String {
        // Get the shared documents directory path
        guard let appFolderPath = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first?.path else {
            fatalError("Unable to get documents directory.")
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
}
