require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))
repository = package['repository']['url'].delete_prefix('git+')

# Kept for apps still on CocoaPods. The trunk only has GoogleSignIn up to 9.2.0 (10.x is SPM
# only, and the trunk goes read-only on 2026-12-02): new apps should use Package.swift.
Pod::Spec.new do |s|
  s.name = 'CapacitorGoogleSignIn'
  s.version = package['version']
  s.summary = package['description']
  s.license = package['license']
  s.homepage = repository.delete_suffix('.git')
  s.author = package['author']
  s.source = { :git => repository, :tag => "v#{s.version}" }
  s.source_files = 'ios/Sources/**/*.swift'
  s.ios.deployment_target = '15.0'
  s.dependency 'Capacitor'
  s.dependency 'GoogleSignIn', '9.2.0'
  s.swift_version = '5.9'
end
