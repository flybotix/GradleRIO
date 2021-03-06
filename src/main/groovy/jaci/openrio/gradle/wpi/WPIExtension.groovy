package jaci.openrio.gradle.wpi

import groovy.json.JsonSlurper
import groovy.transform.CompileStatic
import jaci.openrio.gradle.GradleRIOPlugin
import org.gradle.api.Project

import java.security.MessageDigest

@CompileStatic
class WPIExtension {
    // WPILib (first.wpi.edu/FRC/roborio/maven/release) libs
    String wpilibVersion = "2017.2.1"
    String ntcoreVersion = "3.1.7"
    String opencvVersion = "3.1.0"
    String cscoreVersion = "1.0.1"

    // WPILib C++ Only libs (Note: HAL assumed same version as wpilib)
    String wpiutilVersion = "1.0.2"

    // Third Party (dev.imjac.in/maven/thirdparty) libs
    String ctreVersion = "4.4.1.14"
    String navxVersion = "3.0.323"

    // WPILib (first.wpi.edu/FRC/roborio/maven) Utilities
    String smartDashboardVersion = "2.0.4"
    String javaInstallerVersion = "2.0.3"

    // WPILib Toolchain (first.wpi.edu/FRC/roborio/toolchains) version
    String toolchainVersion = "2017-4.9.3"

    final Project project

    WPIExtension(Project project) {
        this.project = project
        recommended('2017')
    }

    void recommended(String year) {
        def md5 = MessageDigest.getInstance("MD5")
        md5.update(year.bytes)
        def cachename = md5.digest().encodeHex().toString()
        def cachefolder = new File(GradleRIOPlugin.getGlobalDirectory(), "cache/recommended")
        cachefolder.mkdirs()
        def cachefile = new File(cachefolder, cachename)

        String versions_str

        if (project.gradle.startParameter.isOffline()) {
            println "Using offline recommended version cache..."
            versions_str = cachefile.text
        } else {
            try {
                versions_str = "http://openrio.imjac.in/gradlerio/recommended".toURL().text
                cachefile.text = versions_str
            } catch (all) {
                println "Using offline recommended version cache..."
                versions_str = cachefile.text
            }
        }

        def versions = new JsonSlurper().parseText(versions_str)[year] as Map
        this.versions().forEach { String property, Tuple tuple ->
            this.setProperty(property, (versions as Map)[tuple.last()] ?: this.getProperty(property))
        }
    }

    Map<String, Tuple> versions() {
        // Format:
        // property: [ PrettyName, Version, RecommendedKey ]
        return [
            "wpilibVersion" : new Tuple("WPILib", wpilibVersion, "wpilib"),
            "ntcoreVersion" : new Tuple("NTCore", ntcoreVersion, "ntcore"),
            "opencvVersion" : new Tuple("OpenCV", opencvVersion, "opencv"),
            "cscoreVersion" : new Tuple("CSCore", cscoreVersion, "cscore"),

            "wpiutilVersion" : new Tuple("WPIUtil (C++)", wpiutilVersion, "wpiutil"),

            "ctreVersion" : new Tuple("CTRE", ctreVersion, "ctre"),
            "navxVersion" : new Tuple("NavX", navxVersion, "navx"),

            "smartDashboardVersion" : new Tuple("SmartDashboard", smartDashboardVersion, "smartdashboard"),
            "javaInstallerVersion" : new Tuple("JavaInstaller", javaInstallerVersion, "javainstaller"),

            "toolchainVersion" : new Tuple("Toolchain", toolchainVersion, "toolchain")
        ]
    }
}