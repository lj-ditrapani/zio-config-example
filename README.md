ZIO config example
==================

Run the app:

    sbt stage
    # Run in dev
    database_password=dev-secret ./target/universal/stage/bin/zio-config -Dconfig.resource=application-dev.conf
    database_password=prod-secret ./target/universal/stage/bin/zio-config -Dconfig.resource=application-prod.conf
    # Run in prod
    database_password=ext-secret ./target/universal/stage/bin/zio-config -Dconfig.file=application-external.conf

Loads config in srm/main/resources/application-<env>.config according to typesafe config rules and super imposes any config values provided via environment variables.  This allows you to specify secrets as environment variables.
