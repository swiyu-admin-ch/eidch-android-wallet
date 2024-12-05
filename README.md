![24 github_banner-publicbeta v0 2](https://github.com/user-attachments/assets/e0ea1926-5935-43f1-a3b9-38b0bbd2b085)

# Public Beta Wallet - Android

An official Swiss Government project made by the [Federal Office of Information Technology, Systems and Telecommunication FOITT](https://www.bit.admin.ch/)
as part of the electronic identity (E-ID) project.

## Table of Contents

- [Overview](#overview)
- [Installation and building](#installation-and-building)
- [Contributions and feedback](#contributions-and-feedback)
- [License](#license)

## Overview

This repository is part of the ecosystem developed for the future official Swiss E-ID.
The goal of this repository is to engage with the community and collaborate on developing the Swiss ecosystem for E-ID and other credentials.
We warmly encourage you to engage with us by creating an issue in the repository.

For more information about the project please visit the [introduction into open source of the public beta](https://github.com/e-id-admin/eidch-public-beta).

## Installation and building

The app requires at least Android 10 (Q).
To be able to build the project, you need at least Java 17 and Android Studio 2023.1.1.

You can also build the app directly using following command:

```sh
$ ./gradlew app:assembleProdRelease
```

You can then find the generated APK under `app/build/outputs/apk/prod/release/app-prod-release.apk`.

> [!NOTE]
> Please be aware that for building from the command line, you must have set up your own keystore.

## Contributions and feedback

The code for this repository is developed privately and will be released after each sprint. The published code can therefore only be a snapshot of the current development and not a thoroughly tested version. However, we welcome any feedback on the code regarding both the implementation and security aspects. Please follow the guidelines for contributing found in [CONTRIBUTING](./CONTRIBUTING.md).

## License

This project is licensed under the terms of the MIT license. See the [LICENSE](LICENSE) file for details.
