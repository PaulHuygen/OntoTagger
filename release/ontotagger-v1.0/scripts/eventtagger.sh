#java -Xmx812m -cp ../lib/ontotagger-1.0-jar-with-dependencies.jar eu.kyotoproject.main.KafEventTagger --kaf-file "../example/bus-accident.vu_ukb_en.xml" --pos v --predicate-matrix "../resources/predicate-matrix/PredicateMatrix.v0.txt" --version predicatematrix.v0 > "../example/bus-accident.vu_ukb_en.event.kaf"

java -Xmx812m -cp ../lib/ontotagger-1.0-jar-with-dependencies.jar eu.kyotoproject.main.KafEventTagger --kaf-file "../example/bus-accident.vu_ukb_en.wsd.ont.kaf.ont.kaf" --pos v --predicate-matrix "../resources/predicate-matrix/PredicateMatrix.v0.txt" --version predicatematrix.v0 > "../example/bus-accident.vu_ukb_en.wsd.ont.event.kaf"
