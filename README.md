[![GitHub release](https://img.shields.io/github/release/sismics/play-restsecure.svg?style=flat-square)](https://github.com/sismics/play-restsecure/releases/latest)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

# play-restsecure plugin

This module allows simple authentication and authorization management in Play! Framework 1 REST applications.

It is based off the [Play secure](https://playframework.com/documentation/1.4.1/secure) plugin.

# How to use

####  Add the dependency to your `dependencies.yml` file

```
require:
    - restsecure -> restsecure 1.1.0

repositories:
    - sismicsNexusRaw:
        type: http
        artifact: "https://nexus.sismics.com/repository/sismics/[module]-[revision].zip"
        contains:
            - restsecure -> *
```

####  Add the routes to your `routes` file

```
# Secure routes
*       /               module:restsecure
```

####  Customize the authentication mechanism

Extend the class RestSecure.Security and implement the `authenticate` and `check` methods.

####  Secure your controllers

Add the following annotations to secure your controllers:

```
@With({RestSecure.class})
public class MyThings extends Controller {
    @Check("SOME_PRIVILEGE")
    public static void listThings() {
        // ...
    }

}
```

# License

This software is released under the terms of the Apache License, Version 2.0. See `LICENSE` for more
information or see <https://opensource.org/licenses/Apache-2.0>.
