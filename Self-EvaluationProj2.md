# 🧩 Software Sustainability Evaluation – Self-Assessment (Project 2)

| **Category** | **Question** | **Yes** | **No** | **Evidence** |
|--------------|---------------|:-------:|:------:|--------------|
| **Q1 – Software Overview** |||||
| 1.1 | Does your website and documentation provide a clear, high-level overview of your software? | ✅ | | Check the Design.md|
| 1.2 | Does your website and documentation clearly describe the type of user who should use your software? | ✅ | | Check the Repo |
| 1.3 | Do you publish case studies to show how your software has been used by yourself and others? | | ❌ | This is for a class|
| **Q2 – Identity** |||||
| 2.1 | Is the name of your project/software unique? || ❌ | Look up online|
| 2.2 | Is your project/software name free from trademark violations? | ✅ | | Look up online|
| **Q3 – Availability** |||||
| 3.1 | Is your software available as a package that can be deployed without building it? | | ❌ | Must be built|
| 3.2 | Is your software available for free? | ✅ | | It is open-source|
| 3.3 | Is your source code publicly available to download? |  | ❌ | It is still being built |
| 3.4 | Is your software hosted in an established, third-party repository (e.g., GitHub)? | ✅ | | In Github|
| **Q4 – Documentation** |||||
| 4.1 | Is your documentation clearly available on your website or within your software? | ✅ |  |  Lots of documentation|
| 4.2 | Does your documentation include a “quick start” guide? | ✅ |  |  IN ReadME.md and Install.md|
| 4.3 | Does your documentation provide step-by-step deployment and usage instructions? | ✅ | | Check ReadMe.md and Install.md |
| 4.4 | Do you provide a comprehensive guide to all commands, functions, and options? | ✅ |  | Check ReadME.md |
| 4.5 | Do you provide troubleshooting information for errors and problems? | ✅ | | Check REadMe.md|
| 4.6 | Do you provide comprehensive API documentation (if applicable)? | ✅ | | Check API.md|
| 4.7 | Is your documentation under version control with your source code? | ✅ |  | In Github |
| 4.8 | Do you publish your release history (dates, versions, features)? | ✅ |  | In Github |
| **Q5 – Support** |||||
| 5.1 | Does your software describe how users can get help? | ✅ |  | Check ReadMe.md |
| 5.2 | Does your documentation describe what support you provide? | ✅ | |  Check ReadMe.md|
| 5.3 | Does your project have a support e-mail or forum? | ✅ |  | Email one of us or message on Discord|
| 5.4 | Are support e-mails received by more than one person? | ✅ |  | You can email any of us|
| 5.5 | Does your project use a ticketing system for bugs and features? | ✅ |  | We do issues|
| 5.6 | Is your ticketing system publicly visible? | ✅ |  | Yes|
| **Q6 – Maintainability** |||||
| 6.1 | Is your software architecture modular? | ✅ |  | Can changed the database, frontend, etc|
| 6.2 | Does your software follow an accepted coding standard? | ✅ |  | We have checkstyle and spotless|
| **Q7 – Open Standards** |||||
| 7.1 | Does your software use open data formats? |  | ❌ | |
| 7.2 | Does your software support open communication protocols? |  | ❌ | |
| **Q8 – Portability** |||||
| 8.1 | Is your software cross-platform compatible? | ✅ |  | Run in mac and windows |
| **Q9 – Accessibility** |||||
| 9.1 | Does your software adhere to accessibility standards? |  | ❌ | |
| 9.2 | Does your documentation adhere to accessibility standards? |  | ❌ | |
| **Q10 – Source Code Management** |||||
| 10.1 | Is your source code stored in a repository under version control? | ✅ |  | In Github |
| 10.2 | Is each release a snapshot of the repository? | ✅ |  | In Github|
| 10.3 | Are releases tagged in the repository? | ✅ |  | PRs have to approved by people|
| 10.4 | Is there a stable branch (tests always pass)? | ✅ |  | Main|
| 10.5 | Do you back up your repository? | ✅ |  | dev |
| **Q11 – Building & Installing** |||||
| 11.1 | Do you provide public build instructions? |  | ❌ | |
| 11.2 | Can your software be built using an automated tool? | ✅ |  |  Maven|
| 11.3 | Do you provide deployment instructions? | ✅ |  | Check ReadMe.md|
| 11.4 | Does your documentation list all dependencies? | ✅ |  | Pom.xml |
| 11.5 | Does your documentation list dependency versions? | ✅ |  | Pom.xml |
| 11.6 | Does your software list dependency licenses and URLs? |  | ❌ | |
| 11.7 | Can dependencies be downloaded via a package manager? | ✅ |  | Pom.xml|
| 11.8 | Do you have post-build/deployment tests? | ✅ |  |  We have CI tests|
| **Q12 – Testing** |||||
| 12.1 | Do you have an automated test suite? | ✅ |  |  We have CI tests|
| 12.2 | Do you run tests periodically (e.g., nightly)? |  | ❌ | |
| 12.3 | Do you use continuous integration (CI)? | ✅ |  | We have CI tests |
| 12.4 | Are test results publicly visible? | ✅ |  | |
| 12.5 | Are all manual tests documented? | ✅ |  |  They have dipslays|
| **Q13 – Community Engagement** |||||
| 13.1 | Does your project have a regularly updated blog, wiki, or social feed? |  | ❌ | This is for a class|
| 13.2 | Does your website state how many projects/users use your software? |  | ❌ | |
| 13.3 | Do you provide success stories? |  | ❌ | |
| 13.4 | Do you list important partners/collaborators? | ✅ |  | We have badges that |
| 13.5 | Do you list your own publications? |  | ❌ | |
| 13.6 | Do you list third-party publications referencing your software? | ✅ |  | We have badges for what we use|
| 13.7 | Can users subscribe to repository notifications? |  | ❌ | |
| 13.8 | Do you have a public governance model (for open source projects)? |  | ❌ | |
| **Q14 – Contributions** |||||
| 14.1 | Do you accept external contributions? | | ❌ | |
| 14.2 | Do you have a contribution policy? | ✅ |  | |
| 14.3 | Is your contribution policy public? | ✅ |  | |
| 14.4 | Do contributors retain copyright/IP? |  | ❌ |  For a class|
| **Q15 – Licensing** |||||
| 15.1 | Does your documentation clearly state the copyright owners? | ✅ |  | Check License.md|
| 15.2 | Does each source file include a copyright statement? | ✅ |  | Check License.md |
| 15.3 | Does your documentation state your software’s license? | ✅ |  | Check License.md|
| 15.4 | Is your software open source? | ✅ |  | |
| 15.5 | Is your software under an OSI-approved license? | ✅ |  | Check License.md|
| 15.6 | Does each file include a license header? |  | ❌ | Check License.md|
| 15.7 | Do you provide a recommended citation? |  | ❌ | |
| **Q16 – Future Plans** |||||
| 16.1 | Do you include a project roadmap (3–12 months)? | ✅ |  | Check Poster|
| 16.2 | Do you describe funding sources and duration? | ✅ |  | We describe next month|
| 16.3 | Do you announce deprecation of components/APIs in advance? |  | ❌ | |
