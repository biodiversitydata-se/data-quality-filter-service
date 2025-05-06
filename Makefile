run:
	docker compose up --detach pgsql
	./gradlew bootRun

# You need to change dataSource.url in the config.properties
# to use 'pgsql' instead of 'localhost' for this to work
run-docker:
	./gradlew clean bootWar
	docker compose build --no-cache
	docker compose up --detach

release:
	../sbdi-install/utils/make-release.sh
