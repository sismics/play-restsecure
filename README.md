# play-restsecure plugin

This module allows simple authentication and authorization management in Play! Framework 1 REST applications.

It is based off the [Play secure](https://playframework.com/documentation/1.4.1/secure) plugin.

# How to use

####  Add the dependency to your `dependencies.yml` file

```
require:
    - restsecure -> restsecure 0.1

repositories:
    - restsecure:
        type:       http
        artifact:   "http://release.sismics.com/repo/play/[module]-[revision].zip"
        contains:
            - restsecure -> *

```
require:
    - restsecure -> restsecure 0.1
```
####  Add the routes to your `routes` file

```
# Secure routes
*       /               module:restsecure
```

# License

This software is released under the terms of the Apache License, Version 2.0. See `LICENSE` for more
information or see <https://opensource.org/licenses/Apache-2.0>.
