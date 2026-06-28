# Phase II Astah Class Diagram Design Guide

Use this guide to create the refined class diagram in Astah. The goal is to show the original Phase I classes and clearly add the Facade, State, and Command design patterns.

## Recommended Diagram Organization

Place the classes in five visual areas:

1. Boundary/UI classes on the left.
2. Facade and controller classes in the center.
3. Entity classes in the lower center.
4. State Pattern classes on the right side of `Event`.
5. Command Pattern classes near `ClubDashboardPage` and `AdminPanelPage`.

Use stereotypes where possible:

- `<<boundary>>` for UI/page classes.
- `<<control>>` for facade and controller classes.
- `<<entity>>` for domain data classes.
- `<<interface>>` for `EventState` and `DashboardCommand`.

## Boundary/UI Classes

### LoginPage `<<boundary>>`

Attributes:

- `- email : String`
- `- password : String`

Methods:

- `+ displayLoginForm() : void`
- `+ submitLogin(email : String, password : String) : void`
- `+ showLoginError(message : String) : void`

Relationships:

- Dependency from `LoginPage` to `SunDevilConnectFacade`.

### EventSearchPage `<<boundary>>`

Attributes:

- `- searchText : String`
- `- selectedCategory : String`
- `- selectedDate : Date`

Methods:

- `+ searchEvents(keyword : String) : List<Event>`
- `+ applyFilters(category : String, date : Date, location : String) : List<Event>`
- `+ displayEvents(events : List<Event>) : void`

Relationships:

- Dependency from `EventSearchPage` to `SunDevilConnectFacade`.

### EventDetailsPage `<<boundary>>`

Attributes:

- `- selectedEventId : String`

Methods:

- `+ displayEventDetails(eventId : String) : void`
- `+ registerForEvent(studentId : String, eventId : String) : void`
- `+ cancelRegistration(studentId : String, eventId : String) : void`

Relationships:

- Dependency from `EventDetailsPage` to `SunDevilConnectFacade`.

### ClubPage `<<boundary>>`

Attributes:

- `- selectedClubId : String`

Methods:

- `+ displayClub(clubId : String) : void`
- `+ joinClub(studentId : String, clubId : String) : void`
- `+ displayAnnouncements(clubId : String) : void`

Relationships:

- Dependency from `ClubPage` to `SunDevilConnectFacade`.

### ClubDashboardPage `<<boundary>>`

Attributes:

- `- leaderId : String`
- `- selectedClubId : String`

Methods:

- `+ createEvent(eventData : EventData) : void`
- `+ updateEvent(eventId : String, eventData : EventData) : void`
- `+ approveMember(membershipId : String) : void`
- `+ postAnnouncement(clubId : String, message : String) : void`

Relationships:

- Dependency from `ClubDashboardPage` to `SunDevilConnectFacade`.
- Dependency from `ClubDashboardPage` to `DashboardCommand`.

### AdminPanelPage `<<boundary>>`

Attributes:

- `- adminId : String`

Methods:

- `+ approveClub(clubId : String) : void`
- `+ reviewFlaggedContent(contentId : String) : void`
- `+ removeFlaggedContent(contentId : String) : void`

Relationships:

- Dependency from `AdminPanelPage` to `SunDevilConnectFacade`.
- Dependency from `AdminPanelPage` to `DashboardCommand`.

## Facade and Controller Classes

### SunDevilConnectFacade `<<control>>`

Attributes:

- `- authController : AuthController`
- `- eventController : EventController`
- `- clubController : ClubController`
- `- adminController : AdminController`

Methods:

- `+ login(email : String, password : String) : User`
- `+ browseEvents(filter : EventFilter) : List<Event>`
- `+ viewEventDetails(eventId : String) : Event`
- `+ registerForEvent(studentId : String, eventId : String) : EventRegistration`
- `+ cancelEventRegistration(registrationId : String) : void`
- `+ viewClub(clubId : String) : Club`
- `+ joinClub(studentId : String, clubId : String) : ClubMembership`
- `+ executeCommand(command : DashboardCommand) : void`

Relationships:

- Dependency from each UI class to `SunDevilConnectFacade`.
- Association or dependency from `SunDevilConnectFacade` to each controller.

### AuthController `<<control>>`

Attributes:

- `- users : List<User>`

Methods:

- `+ authenticate(email : String, password : String) : User`
- `+ authorize(user : User, action : String) : boolean`
- `+ logout(userId : String) : void`

Relationships:

- Association to `User`.

### EventController `<<control>>`

Attributes:

- `- events : List<Event>`
- `- registrations : List<EventRegistration>`

Methods:

- `+ searchEvents(filter : EventFilter) : List<Event>`
- `+ getEventDetails(eventId : String) : Event`
- `+ registerStudent(student : Student, event : Event) : EventRegistration`
- `+ cancelRegistration(registrationId : String) : void`
- `+ createEvent(eventData : EventData) : Event`
- `+ updateEvent(eventId : String, eventData : EventData) : void`

Relationships:

- Association to `Event`.
- Association to `EventRegistration`.
- Uses `EventState` through `Event`.

### ClubController `<<control>>`

Attributes:

- `- clubs : List<Club>`
- `- memberships : List<ClubMembership>`

Methods:

- `+ getClub(clubId : String) : Club`
- `+ requestMembership(student : Student, club : Club) : ClubMembership`
- `+ approveMembership(membershipId : String) : void`
- `+ postAnnouncement(clubId : String, message : String) : Announcement`

Relationships:

- Association to `Club`.
- Association to `ClubMembership`.
- Association to `Announcement`.

### AdminController `<<control>>`

Attributes:

- `- flaggedItems : List<FlaggedContent>`

Methods:

- `+ approveClub(clubId : String) : void`
- `+ reviewFlaggedContent(contentId : String) : FlaggedContent`
- `+ removeFlaggedContent(contentId : String) : void`
- `+ suspendClub(clubId : String) : void`

Relationships:

- Association to `Club`.
- Association to `FlaggedContent`.

## Entity Classes

### User `<<entity>>`

Mark this as abstract.

Attributes:

- `# userId : String`
- `# name : String`
- `# email : String`
- `# passwordHash : String`

Methods:

- `+ login() : void`
- `+ logout() : void`
- `+ updateProfile(name : String, email : String) : void`

Relationships:

- Generalization parent of `Student`, `ClubLeader`, and `Administrator`.

### Student `<<entity>>`

Attributes:

- `- major : String`
- `- graduationYear : int`

Methods:

- `+ browseEvents(filter : EventFilter) : List<Event>`
- `+ registerForEvent(event : Event) : EventRegistration`
- `+ cancelRegistration(registration : EventRegistration) : void`
- `+ joinClub(club : Club) : ClubMembership`

Relationships:

- Inherits from `User`.
- Association to `EventRegistration`.
- Association to `ClubMembership`.

### ClubLeader `<<entity>>`

Attributes:

- `- managedClubIds : List<String>`

Methods:

- `+ createEvent(club : Club, eventData : EventData) : Event`
- `+ updateEvent(event : Event, eventData : EventData) : void`
- `+ approveMember(membership : ClubMembership) : void`
- `+ postAnnouncement(club : Club, message : String) : Announcement`

Relationships:

- Inherits from `User`.
- Association to `Club`.

### Administrator `<<entity>>`

Attributes:

- `- adminLevel : String`

Methods:

- `+ approveClub(club : Club) : void`
- `+ reviewFlaggedContent(content : FlaggedContent) : void`
- `+ removeContent(content : FlaggedContent) : void`

Relationships:

- Inherits from `User`.
- Association to `FlaggedContent`.

### Club `<<entity>>`

Attributes:

- `- clubId : String`
- `- name : String`
- `- description : String`
- `- category : String`
- `- status : String`

Methods:

- `+ addEvent(event : Event) : void`
- `+ removeEvent(eventId : String) : void`
- `+ addMember(membership : ClubMembership) : void`
- `+ postAnnouncement(announcement : Announcement) : void`

Relationships:

- Composition from `Club` to `Event`.
- Composition from `Club` to `Announcement`.
- Aggregation or association from `Club` to `ClubMembership`.
- Association from `ClubLeader` to `Club`.

### Event `<<entity>>`

Attributes:

- `- eventId : String`
- `- title : String`
- `- description : String`
- `- category : String`
- `- dateTime : Date`
- `- location : String`
- `- capacity : int`
- `- registrationCount : int`
- `- fee : double`
- `- currentState : EventState`

Methods:

- `+ publish() : void`
- `+ registerStudent(student : Student) : EventRegistration`
- `+ cancel() : void`
- `+ updateDetails(eventData : EventData) : void`
- `+ setState(state : EventState) : void`
- `+ isFull() : boolean`

Relationships:

- Association from `Event` to `EventState`.
- Association from `Event` to `EventRegistration`.
- Composition from `Club` to `Event`.

### ClubMembership `<<entity>>`

Attributes:

- `- membershipId : String`
- `- studentId : String`
- `- clubId : String`
- `- status : String`
- `- requestDate : Date`

Methods:

- `+ approve() : void`
- `+ reject() : void`
- `+ cancel() : void`

Relationships:

- Association to `Student`.
- Association to `Club`.

### EventRegistration `<<entity>>`

Attributes:

- `- registrationId : String`
- `- studentId : String`
- `- eventId : String`
- `- registrationDate : Date`
- `- status : String`

Methods:

- `+ confirm() : void`
- `+ cancel() : void`
- `+ markAttended() : void`

Relationships:

- Association to `Student`.
- Association to `Event`.

### Announcement `<<entity>>`

Attributes:

- `- announcementId : String`
- `- clubId : String`
- `- title : String`
- `- message : String`
- `- postedDate : Date`

Methods:

- `+ publish() : void`
- `+ edit(message : String) : void`
- `+ archive() : void`

Relationships:

- Composition from `Club` to `Announcement`.

### FlaggedContent `<<entity>>`

Attributes:

- `- contentId : String`
- `- reporterId : String`
- `- reason : String`
- `- status : String`
- `- reportedDate : Date`

Methods:

- `+ markReviewed() : void`
- `+ remove() : void`
- `+ dismiss() : void`

Relationships:

- Association from `Administrator` to `FlaggedContent`.
- Association from `AdminController` to `FlaggedContent`.

## State Pattern Classes

### EventState `<<interface>>`

Methods:

- `+ publish(event : Event) : void`
- `+ registerStudent(event : Event, student : Student) : EventRegistration`
- `+ cancel(event : Event) : void`
- `+ updateDetails(event : Event, eventData : EventData) : void`

Relationships:

- Realization from each concrete event state class to `EventState`.
- Association from `Event` to `EventState`.

### DraftEventState

Methods:

- `+ publish(event : Event) : void`
- `+ registerStudent(event : Event, student : Student) : EventRegistration`
- `+ cancel(event : Event) : void`
- `+ updateDetails(event : Event, eventData : EventData) : void`

Rules:

- Allows editing and publishing.
- Does not allow student registration.

### PublishedEventState

Methods:

- `+ publish(event : Event) : void`
- `+ registerStudent(event : Event, student : Student) : EventRegistration`
- `+ cancel(event : Event) : void`
- `+ updateDetails(event : Event, eventData : EventData) : void`

Rules:

- Allows registration.
- Transitions to `FullEventState` when capacity is reached.

### FullEventState

Methods:

- `+ publish(event : Event) : void`
- `+ registerStudent(event : Event, student : Student) : EventRegistration`
- `+ cancel(event : Event) : void`
- `+ updateDetails(event : Event, eventData : EventData) : void`

Rules:

- Rejects normal registration.
- Allows cancellation by club leader.

### CancelledEventState

Methods:

- `+ publish(event : Event) : void`
- `+ registerStudent(event : Event, student : Student) : EventRegistration`
- `+ cancel(event : Event) : void`
- `+ updateDetails(event : Event, eventData : EventData) : void`

Rules:

- Rejects registration and most edits.

## Command Pattern Classes

### DashboardCommand `<<interface>>`

Methods:

- `+ execute() : void`
- `+ getCommandName() : String`

Relationships:

- Realization from each command class to `DashboardCommand`.
- Dependency from `SunDevilConnectFacade` to `DashboardCommand`.

### CreateEventCommand

Attributes:

- `- leader : ClubLeader`
- `- club : Club`
- `- eventData : EventData`
- `- eventController : EventController`

Methods:

- `+ execute() : void`
- `+ getCommandName() : String`

### UpdateEventCommand

Attributes:

- `- leader : ClubLeader`
- `- event : Event`
- `- eventData : EventData`
- `- eventController : EventController`

Methods:

- `+ execute() : void`
- `+ getCommandName() : String`

### ApproveMemberCommand

Attributes:

- `- leader : ClubLeader`
- `- membership : ClubMembership`
- `- clubController : ClubController`

Methods:

- `+ execute() : void`
- `+ getCommandName() : String`

### PostAnnouncementCommand

Attributes:

- `- leader : ClubLeader`
- `- club : Club`
- `- message : String`
- `- clubController : ClubController`

Methods:

- `+ execute() : void`
- `+ getCommandName() : String`

### ReviewFlaggedContentCommand

Attributes:

- `- administrator : Administrator`
- `- flaggedContent : FlaggedContent`
- `- decision : String`
- `- adminController : AdminController`

Methods:

- `+ execute() : void`
- `+ getCommandName() : String`

## Relationship Checklist for Astah

Draw these relationships clearly:

- `Student`, `ClubLeader`, and `Administrator` inherit from abstract `User`.
- All UI classes have dependency arrows to `SunDevilConnectFacade`.
- `SunDevilConnectFacade` has associations or dependencies to `AuthController`, `EventController`, `ClubController`, and `AdminController`.
- `ClubLeader` is associated with `Club`.
- `Club` has composition relationships to `Event` and `Announcement`.
- `Student` is associated with `EventRegistration` and `ClubMembership`.
- `EventRegistration` is associated with `Event`.
- `ClubMembership` is associated with `Club`.
- `Administrator` is associated with `FlaggedContent`.
- `Event` is associated with `EventState`.
- `DraftEventState`, `PublishedEventState`, `FullEventState`, and `CancelledEventState` realize `EventState`.
- `CreateEventCommand`, `UpdateEventCommand`, `ApproveMemberCommand`, `PostAnnouncementCommand`, and `ReviewFlaggedContentCommand` realize `DashboardCommand`.
- `SunDevilConnectFacade` depends on `DashboardCommand` through `executeCommand(command : DashboardCommand)`.

## Astah Notation Tips

- Use hollow triangle arrows for inheritance from `User`.
- Use dashed hollow triangle arrows for interface realization from concrete state/command classes.
- Use dependency arrows from UI pages to the facade.
- Use filled diamond composition from `Club` to `Event` and `Announcement`.
- Add multiplicities where helpful:
  - `Club 1` to `Event 0..*`
  - `Club 1` to `Announcement 0..*`
  - `Student 1` to `EventRegistration 0..*`
  - `Event 1` to `EventRegistration 0..*`
  - `Student 1` to `ClubMembership 0..*`
  - `Club 1` to `ClubMembership 0..*`
