
millcomp: 
	mill demo.compile

millrtl:
	mill demo.runMain demo.genrtl

sbtcomp: 
	sbt demo.compile

sbtrtl:
	sbt "runMain demo.genrtl"

