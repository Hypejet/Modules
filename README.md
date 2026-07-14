# Modules
A Java library making modular programming easier. Made for Hypejet Network, now available to everyone.

## Use cases
This library was made with many use cases in mind, for example:
- **Modular monolith architecture** - you can use Modules to split features of your project into multiple independant modules. This allows the application to keep running, even if one of those modules fail.
- **Creating and removing complex environments at any time during runtime** - for example, Hypejet uses this library to create and load minigame rounds, which are then fully unloaded and removed.
- **And anywhere you need modules!** - Modules was designed to be feature-complete and provide advanced features, such as abstract modules or dependency overriding. You can learn more about them on the wiki.

## Why Modules?
Here's some advantages Modules has over similar libraries:
- **Lightweight** - weighs less than 20kB.
- **Ease of use** - basic usage is really simple, you can create your first modular environment in minutes!
- **Fast** - thanks to its simplicity, the module loading mechanism itself is really fast. In practice however, the overall speed depends on how fast your modules load and unload.
- **Keeps dependencies simple** - requires only plain Java 25 and [SLF4J](https://www.slf4j.org), which is compatible with almost every common logging framework and is widely used nowadays.
- **Fail-safe** - if any module fails to register or load, no exception is thrown, and your application continues to run. The only exception is when circular module loading is found, but it fails early and is easy to detect, you can find out more about it on the wiki.
- **Feature-complete** - Modules was designed not only for Hypejet, it can provide more advanced features where needed.

## Usage
You can find out how to use Modules on our [wiki](https://github.com/Hypejet/Modules/wiki).

## Contributing
Pull requests are welcome. There are no strict requirements for now, however - if possible - please try to follow our code style. But don't worry, in case there's something wrong, it will most likely get corrected by maintainers.

## License
This project is licensed under the terms of [Apache 2.0 License](LICENSE).
