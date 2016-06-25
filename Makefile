.PHONY: release

release:
	lein clean
	lein cljsbuild once min
	cp -R resources/public/* .
