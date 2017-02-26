# metaheuristics

A Java library vending simple, reusable metaheuristics for optimization.

Currently supported algorithms:

* Genetic Algorithms

Planned algorithms:

* Simulated Annealing (converted from my Clojure implementation)
* Beam Search

## Usage

See "src/samples" for examples using these.

To use with maven locally, checkout out the repo and run `mvn install`.

Alternatively, generate a jar with `mvn package` and find the jar in "target".

TODO Coming soon, maven coordinates.

## TODO

### General

* Simulated Annealing
* Beam Search
* Deploy as jar to maven

### Genetic Algorithms

* Support configurable selection strategies. Aside from culling, there is currently no weighting towards more fit parents.
* A parallel version which can exchange "elite children" across generations. Basically, the concept of cross-pollination.

## Changelog

### 1.0

* Initial release, includes support for genetic algorithms

## License

Copyright © 2017 blandflakes

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

