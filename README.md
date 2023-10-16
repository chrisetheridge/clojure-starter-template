# Clojure starter template

This is a work-in-progress starter template, using an (opinionated) modern stack:

- [Integrant](https://github.com/weavejester/integrant) for managing systems and their depdendencies.
- [Aero](https://github.com/juxt/aero/) for config management.
- [Datomic](https://www.datomic.com/) for the database (which may become xtdb).
- [Reitit](https://github.com/metosin/reitit) for route handling.
- [Ring](https://github.com/sunng87/ring-jetty9-adapter) for the web server (which may become http-kit or something else).
- [Babashka](https://github.com/babashka/babashka) for scripting.
- [shadow-cljs](https://github.com/thheller/shadow-cljs/commits/master) for Clojurescript compilation.
- [UIx](https://github.com/pitch-io/uix) as a React wrapper.

Eventually this will be a template that you can start from, using [clj-new].

# Running

Currently you can run the `dev` system using `bb`:

``` sh
bb dev
```

This will start an `nrepl` on port 9993, set up a Datomic connection, and start the web server. 
