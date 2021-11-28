cljs:
	shadow-cljs -A:f3-dev:rad-dev:i18n-dev server

report:
	npx shadow-cljs run shadow.cljs.build-report main report.html

release:
	TIMBRE_LEVEL=:warn npx shadow-cljs release main

server:
	clj -A:dev:datomic -M -m com.example.components.server

test-datomic:
	clj -A:dev:datomic:test:run-tests

test-xtdb:
	clj -A:dev:xtdb:test:run-tests

test-sql:
	clj -A:dev:sql:test:run-tests
