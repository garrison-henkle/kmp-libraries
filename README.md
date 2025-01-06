# Garrison's Miscellaneous Kotlin Multiplatform Libraries

This is a random collection of KMP libraries that I work on for fun. This repo has
existed as a private repo for years at this point, but I've hesitated to make it
public because of a) the hacky nature of most of this work b) the lack of tests and
c) the lack of documentation. I've decided to make it public anyway, and will try to
improve the documentation, testing, and processes (e.g. put my work into PRs like a proper
human) in the near future to make more of these libraries production ready. In the meantime,
use the below table to get a sense of what is and isn't ready for the limelight (note that some
of these are not hosted in this repo):

| Library                      | Directory / GitHub Repository                       | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   | Support Status             | Production Ready                                        | Tests | Future goals / plans                                                                                                                                                                                                                                                                                                                                                                                          |
|------------------------------|-----------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------|---------------------------------------------------------|-------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| Context Provider             | context-provider                                    | Android helper library to make it easy to get access to application `Context` using a `ContentProvider`. Mostly borrowed from the [Firebase Android SDK](https://github.com/firebase/firebase-android-sdk/blob/f024090abdc9e80a91cd540abf080995450c1d6a/firebase-common/src/main/java/com/google/firebase/provider/FirebaseInitProvider.java)                                                                                                                                                                                                                                                 | Supported                  | Probably safe                                           | No    | Maybe some tests, but unlikely that I'll make future changes to this library unless ContentProviders are broken in a future version of Android                                                                                                                                                                                                                                                                |
| Descope KMP                  | https://github.com/garrison-henkle/descope-kotlin   | A KMP port of Descope Kotlin that introduces minimal API changes to the original Android source set                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | Deprecated                 | No                                                      | No    | I don't currently have a use for this library anymore, so I don't plan on finishing the port unless I feel inspired to do it one afternoon for the sake of learning more about Android library -> KMP library ports                                                                                                                                                                                           |
| KeyMP                        | keymp                                               | Wrapper around different key-value storage methods on Apple, Android, JVM, and JS/WasmJs                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                      | Supported                  | No                                                      | No    | Adding thorough tests for all platforms                                                                                                                                                                                                                                                                                                                                                                       |
| KeyMP Preferences            | keymp-preferences                                   | A reactive settings/preferences wrapper around KeyMP                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          | Supported                  | No                                                      | No    | Adding through tests for all platforms + fixing a bug that causes default values to never be saved to disk when used for the first time                                                                                                                                                                                                                                                                       |
| KMarkdownP                   | kmarkdownp                                          | A markdown parsing library that transforms abstract syntax trees (ASTs) created by various parsers (see below libraries) into an easy-to-use intermediate representation (IR)                                                                                                                                                                                                                                                                                                                                                                                                                 | Supported / In Development | Experimental (used in production at You.com)            | No    | Adding through tests and stabilizing the API once the tree-sitter implementation is finished. Possibly adding a parser that uses the CommonMark C reference implementation, CMark, in the future as well                                                                                                                                                                                                      |
| KMarkdownP Jetbrains Parser  | kmarkdownp-parser-jetbrains                         | A parser for KMarkdownP powered by [Jetbrains/markdown](https://github.com/JetBrains/markdown), a KMP markdown library that supports all targets                                                                                                                                                                                                                                                                                                                                                                                                                                              | Supported                  | Experimental (used in production at You.com)            | No    | Rewriting the entire parser, as its probably the worst piece of code in this repo. Adding tests to the re-written parser                                                                                                                                                                                                                                                                                      |
| KMarkdownP Treesitter Parser | kmarkdownp-parser-jetbrains                         | A parser for KMarkdownP powered by [Tree-sitter](https://tree-sitter.github.io/tree-sitter/), a general incremental parser written in C. Uses the [tree-sitter-markdown](https://github.com/tree-sitter-grammars/tree-sitter-markdown) grammar and a Kotlin port of the [Rust parser implementation](https://github.com/tree-sitter-grammars/tree-sitter-markdown/blob/192407ab5a24bfc24f13332979b5e7967518754a/bindings/rust/parser.rs) found in the grammar's repo.                                                                                                                         | In Development             | No                                                      | No    | This will be the new default parser for KMarkdownP, as it is incremental and generally faster than the Jetbrains parser                                                                                                                                                                                                                                                                                       |
| KMarkdownP UI                | kmarkdownp-ui                                       | A Compose Multiplatform markdown renderer built on top of KMarkdownP that converts the KMarkdownP intermediate representation (IR) into a more Compose-friendly UI IR before rendering the UI IR. Designed to address the flaws of [multiplatform-markdown-renderer](https://github.com/mikepenz/multiplatform-markdown-renderer), such as the ability to easy replace the UI components (since it gives raw syntax tree nodes); support for LaTeX, tables, nested markdown nodes, and other markdown elements unsupported by multiplatform-markdown-renderer; and swappable markdown parsers | Supported                  | Experimental (used in production at You.com)            | No    | Adding unit tests for the UI IR generator and adding snapshot tests for the UI                                                                                                                                                                                                                                                                                                                                |
| KMPayments                   | kmpayments                                          | A KMP wrapper around StoreKit (iOS) and Google Play Billing (Android).                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        | Unsupported / Unfinished   | No                                                      | No    | I'll revisit this when I have the need for payments in the future                                                                                                                                                                                                                                                                                                                                             |
| KMPress                      | kmpress                                             | A Compose Multiplatform library to mimic iOS' long press behavior that stretches the content to fill the width of the screen and blurs the background (e.g. long pressing on a message in iMessage)                                                                                                                                                                                                                                                                                                                                                                                           | Unsupported / Not Started  | No                                                      | No    | I'll revisit this when I have a need for this behavior in one of my projects in the future                                                                                                                                                                                                                                                                                                                    |
| Korvus                       | korvus                                              | A KMP driver for the [RavenDB](https://ravendb.net/) database                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                 | Partial Support            | Probably safe                                           | Yes   | I ended up going with SurrealDB as the database for my personal project instead of RavenDB, but I'll build this out more if I ever find a use for RavenDB in the future                                                                                                                                                                                                                                       |
| MacOS Screenshots            | macos-screenshots                                   | A wrapper around MacOS's screenshot API for KMP                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               | Unsupported / Unfinished   | No                                                      | No    | Originally intended for use in a personal project to allow for use of a screen's contents as context to an AI. I abandoned the project and have no intentions of ever revisiting, but I'm leaving the code here just in case                                                                                                                                                                                  |
| Moko Geo                     | https://github.com/garrison-henkle/moko-geo         | A fork of moko-geo that supports Android's `LocationManager`                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | Deprecated                 | Experimental (was used in production at You.com)        | No    |                                                                                                                                                                                                                                                                                                                                                                                                               |
| Moko Permissions             | https://github.com/garrison-henkle/moko-permissions | A fork of moko-permissions that supports Android 13's partial location grants (e.g. request precise location and use only grants coarse location)                                                                                                                                                                                                                                                                                                                                                                                                                                             | Deprecated                 | Experimental (was used in production at You.com)        | No    |                                                                                                                                                                                                                                                                                                                                                                                                               |
| Nano ID                      | nanoid                                              | A copy-paste of [DatL4g's KMP implementation](https://github.com/DatL4g/KMP-NanoId) of Nano ID that I slightly reworked to work the way I want it to                                                                                                                                                                                                                                                                                                                                                                                                                                          | Supported                  | Yes                                                     | Yes   |                                                                                                                                                                                                                                                                                                                                                                                                               |
| Pager                        | pager                                               | A simple infinite pager Composable for Compose Multiplatform                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  | Supported                  | Yes, but it's not great (used in production at You.com) | No    | I've always found paging libraries to be unnecessarily complex, but there seems to be very few real world use-cases for such a simple pager. Considering this library is a bit too simplistic, has no tests, and honestly isn't very good at paging smoothly, I'd highly recommend using Cashapp's [multiplatform-paging](https://github.com/cashapp/multiplatform-paging) port of the AndroidX Pager instead |
| Stytch KMP                   | stytch-kmp                                          | A barebones KMP SDK for interacting with the [Stytch](https://stytch.com/) authentication service                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | Deprecated                 | No                                                      | No    | Originally intended for use in a personal project that I ended up abandoning, so this library is untested and may or may not be feature complete. It's already really out of date when I'm writing this, so just use the official SDKs and build your own authentication wrapper in KMP as needed                                                                                                             |
| SurrealKMP                   | surreal-kmp                                         | A KMP driver for the [SurrealDB](https://surrealdb.com/) database                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | Supported                  | Yes                                                     | Yes   | SurrealDB is my favorite low-effort database at the moment, so this should hopefully be kept up-to-date with the latest database versions                                                                                                                                                                                                                                                                     |
| Swift KLib Plugin Fork       | swift-klib-plugin                                   | Fork of the [swift-klib-plugin](https://github.com/ttypic/swift-klib-plugin) that was updated to work with the latest Kotlin versions (that were unsupported at the time)                                                                                                                                                                                                                                                                                                                                                                                                                     | Deprecated                 | No                                                      | No    | Originally used for the `StoreKit` code in `kmpayments`, but I haven't maintained it whatsoever since making my original changes. Try the original project instead, as it seems to be somewhat maintained                                                                                                                                                                                                     |
| Test Utils                   | test-utils                                          | A random collection of utility functions and classes that I like using in tests                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               | Supported?                 | No                                                      | No    | This will get periodically updated with functions shared among the above libraries                                                                                                                                                                                                                                                                                                                            |
| Utils                        | utils                                               | A random collection of utility functions and classes that I like using in my projects                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         | Supported?                 | No                                                      | No    | This will get periodically updated with functions shared among the above libraries                                                                                                                                                                                                                                                                                                                            |

# Future Improvements
This monorepo primarily exists as a sandbox where I can avoid dealing with things like CI/CD, Maven repos,
and PRs when I'm coding for fun, so it's unlikely that I'll *completely* switch over to the
proper way of developing with branches and PRs anytime soon.

That being said, I want some of these libraries to be production ready in the future. To that end,
I'm going to try to do the following moving forward to make this repo less of a mess (last updated 1/5/24):
[ ] a `main` branch will be created with any releases, and only functioning code will be pushed to that branch after it is created
[ ] GitHub releases for each new library version
[ ] my "stream of consciousness" coding of random commits to random libraries whenever I feel like it will occur in a new `dev` branch
[ ] for libraries that I have given the status "production ready" in the table above, I'll make proper branches and PRs for all updates
[ ] start uploading new release to maven central
 - all releases are currently sent out to my personal private maven repository, so it's hard for anyone but me to use anything in this repo
[ ] simple CI/CD for publishing all of the libraries using GitHub Actions
[ ] revamp of the convention plugins so they can be published

# License

Unless otherwise declared with a directory-specific LICENSE file or licenses in comments at the top of a file, the code in this project
is distributed under an Apache 2 license:

Copyright 2024-2025 Garrison Henkle

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.