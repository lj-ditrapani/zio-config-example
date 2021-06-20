ZIO config example
==================

This is an example ZIO app that demos how you might use zio-config and ZLayer in a real app.
It has 2 mock dependencies (database & upstreamApi) that need to be configured.

Run the app:

    sbt stage
    # Run in dev
    database_password=dev-secret ./target/universal/stage/bin/zio-config -Dconfig.resource=application-dev.conf
    # Run in prod
    database_password=prod-secret ./target/universal/stage/bin/zio-config -Dconfig.resource=application-prod.conf
    # Run with external config
    database_password=ext-secret ./target/universal/stage/bin/zio-config -Dconfig.file=application-external.conf

Loads config in srm/main/resources/application-<env>.config according to typesafe config rules and super imposes any config values provided via environment variables.  This allows you to specify secrets as environment variables outside of your baked-in config file.
