def gitCommit
def scriptDir="/var/lib/jenkins/scripts"
def repoName="data-catalog-backend"
def repoBranch="master"
def organization="navikt"
def appId="26100" // Defined in the GitHub App "datajegerne"
def checkedOutLibraryScriptsRoot = "./../data-catalog-backend@libs/"
//
// =============================================================================
// Set when explicitly loading groovy snippets from SCM:
//
def dockerUtilsScript
def naisScript
def slackScript
def versionScript
//
// =============================================================================
//
def checkOutLibrary(final String scriptDir, final String organization, final String repoName, final String repoBranch, final String libraryName, final String appId) {
	def checkedOutLibraryScriptRoot =
		sh (
		   script      : scriptDir + '/pull.via.github.app/pull-shared-pipeline-scripts-repo-using-GitHub-App.sh \'' + organization + '\' \'' + repoName + '\' \'' + repoBranch + '\' \'' + appId + '\' \'' + libraryName + '\'',
		   returnStdout: true
		).trim()
	return checkedOutLibraryScriptRoot;
}

def loadLibraryScript(final String checkedOutLibraryScriptRoot, final String libraryScriptName) {
	return load(checkedOutLibraryScriptRoot + '/vars/' + libraryScriptName + '.groovy')
}

pipeline {
    agent any

    tools {
        maven "maven-3.3.9"
        jdk "java11"
    }

    stages {
        stage("Load libraries") {
            steps {
                script {
					def checkedOutLibraryScriptRoot = checkOutLibrary(scriptDir, organization, 'jenkins-datajegerne-pipeline', 'master', 'pipeline-lib', appId)
                    echo "About to load libraries..."
                    dockerUtilsScript = loadLibraryScript(checkedOutLibraryScriptRoot, 'dockerUtils')
                    naisScript        = loadLibraryScript(checkedOutLibraryScriptRoot, 'nais'       )
                    slackScript       = loadLibraryScript(checkedOutLibraryScriptRoot, 'slack'      )
                    versionScript     = loadLibraryScript(checkedOutLibraryScriptRoot, 'version'    )
                }
            }
        }

        stage("Checkout application") {
            steps {
                script {
                   gitCommit = sh (
                       script      : scriptDir + '/pull.via.github.app/pull-app-repo-using-GitHub-App.sh \'' + organization + '\' \'' + repoName + '\' \'' + repoBranch + '\' \'' + appId + '\'',
                       returnStdout: true
                   ).trim()
                }
            }
        }
    }
}