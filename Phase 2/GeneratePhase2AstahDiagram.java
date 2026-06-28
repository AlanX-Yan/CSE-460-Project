import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.change_vision.jude.api.inf.AstahAPI;
import com.change_vision.jude.api.inf.editor.BasicModelEditor;
import com.change_vision.jude.api.inf.editor.ClassDiagramEditor;
import com.change_vision.jude.api.inf.editor.ModelEditorFactory;
import com.change_vision.jude.api.inf.editor.TransactionManager;
import com.change_vision.jude.api.inf.model.IAssociation;
import com.change_vision.jude.api.inf.model.IClass;
import com.change_vision.jude.api.inf.model.IDependency;
import com.change_vision.jude.api.inf.model.INamedElement;
import com.change_vision.jude.api.inf.model.IGeneralization;
import com.change_vision.jude.api.inf.model.IModel;
import com.change_vision.jude.api.inf.model.IPackage;
import com.change_vision.jude.api.inf.model.IRealization;
import com.change_vision.jude.api.inf.presentation.INodePresentation;
import com.change_vision.jude.api.inf.project.ProjectAccessor;

public class GeneratePhase2AstahDiagram {
    private static BasicModelEditor modelEditor;
    private static ClassDiagramEditor diagramEditor;
    private static IPackage pkg;
    private static final Map<String, IClass> classes = new LinkedHashMap<>();
    private static final Map<String, INodePresentation> nodes = new LinkedHashMap<>();
    private static final List<IGeneralization> generalizations = new ArrayList<>();
    private static final List<IRealization> realizations = new ArrayList<>();
    private static final List<IDependency> dependencies = new ArrayList<>();
    private static final List<IAssociation> associations = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        String outPath = args.length > 0 ? args[0] : "Phase2_SunDevilConnect_ClassDiagram.asta";
        System.out.println("Starting Astah generator");
        File outFile = new File(outPath);
        if (outFile.exists() && !outFile.delete()) {
            throw new RuntimeException("Could not replace existing file: " + outFile.getAbsolutePath());
        }

        ProjectAccessor accessor = AstahAPI.getAstahAPI().getProjectAccessor();
        System.out.println("Creating project: " + outFile.getAbsolutePath());
        accessor.create(outFile.getAbsolutePath());
        System.out.println("Project created");
        IModel project = accessor.getProject();

        try {
            TransactionManager.beginTransaction();
            modelEditor = ModelEditorFactory.getBasicModelEditor();
            diagramEditor = accessor.getDiagramEditorFactory().getClassDiagramEditor();
            pkg = modelEditor.createPackage(project, "SunDevil Connect Phase II");

            System.out.println("Creating classes");
            createClasses();
            System.out.println("Creating relationships");
            createRelationships();
            System.out.println("Creating diagram");
            createDiagram();

            TransactionManager.endTransaction();
            System.out.println("Saving project");
            accessor.save();
            accessor.close();
            System.out.println("Created " + outFile.getAbsolutePath());
        } catch (Throwable t) {
            System.err.println("Generator failed: " + t.getClass().getName() + " - " + t.getMessage());
            t.printStackTrace(System.err);
            TransactionManager.abortTransaction();
            throw t;
        }
    }

    private static IClass cls(String name, String stereotype, String[] attrs, String[] ops) throws Exception {
        System.out.println("  class " + name);
        IClass c = findClass(name);
        if (c == null) {
            c = modelEditor.createClass(pkg, name);
        }
        if (stereotype != null && !stereotype.isEmpty()) {
            c.setDefinition("<<" + stereotype + ">>");
        }
        for (String attr : attrs) {
            String[] parts = splitMember(attr);
            modelEditor.createAttribute(c, parts[0], parts[1]);
        }
        for (String op : ops) {
            String[] parts = splitMember(op);
            modelEditor.createOperation(c, parts[0], parts[1]);
        }
        classes.put(name, c);
        return c;
    }

    private static IClass iface(String name, String[] ops) throws Exception {
        System.out.println("  interface " + name);
        IClass c = findClass(name);
        if (c == null) {
            c = modelEditor.createInterface(pkg, name);
        }
        c.setDefinition("<<interface>>");
        for (String op : ops) {
            String[] parts = splitMember(op);
            modelEditor.createOperation(c, parts[0], parts[1]);
        }
        classes.put(name, c);
        return c;
    }

    private static IClass findClass(String name) {
        if (classes.containsKey(name)) {
            return classes.get(name);
        }
        for (INamedElement element : pkg.getOwnedElements()) {
            if (element instanceof IClass && name.equals(element.getName())) {
                return (IClass) element;
            }
        }
        return null;
    }

    private static String[] splitMember(String value) {
        int idx = value.indexOf(" : ");
        if (idx < 0) {
            return new String[] { value, "void" };
        }
        return new String[] { value.substring(0, idx), value.substring(idx + 3) };
    }

    private static void createClasses() throws Exception {
        cls("LoginPage", "boundary",
            new String[] {"email : String", "password : String"},
            new String[] {"displayLoginForm() : void", "submitLogin(email, password) : void", "showLoginError(message) : void"});
        cls("EventSearchPage", "boundary",
            new String[] {"searchText : String", "selectedCategory : String", "selectedDate : Date"},
            new String[] {"searchEvents(keyword) : List<Event>", "applyFilters(category, date, location) : List<Event>", "displayEvents(events) : void"});
        cls("EventDetailsPage", "boundary",
            new String[] {"selectedEventId : String"},
            new String[] {"displayEventDetails(eventId) : void", "registerForEvent(studentId, eventId) : void", "cancelRegistration(studentId, eventId) : void"});
        cls("ClubPage", "boundary",
            new String[] {"selectedClubId : String"},
            new String[] {"displayClub(clubId) : void", "joinClub(studentId, clubId) : void", "displayAnnouncements(clubId) : void"});
        cls("ClubDashboardPage", "boundary",
            new String[] {"leaderId : String", "selectedClubId : String"},
            new String[] {"createEvent(eventData) : void", "updateEvent(eventId, eventData) : void", "approveMember(membershipId) : void", "postAnnouncement(clubId, message) : void"});
        cls("AdminPanelPage", "boundary",
            new String[] {"adminId : String"},
            new String[] {"approveClub(clubId) : void", "reviewFlaggedContent(contentId) : void", "removeFlaggedContent(contentId) : void"});

        cls("SunDevilConnectFacade", "control",
            new String[] {"authController : AuthController", "eventController : EventController", "clubController : ClubController", "adminController : AdminController"},
            new String[] {"login(email, password) : User", "browseEvents(filter) : List<Event>", "viewEventDetails(eventId) : Event", "registerForEvent(studentId, eventId) : EventRegistration", "cancelEventRegistration(registrationId) : void", "viewClub(clubId) : Club", "joinClub(studentId, clubId) : ClubMembership", "executeCommand(command) : void"});
        cls("AuthController", "control",
            new String[] {"users : List<User>"},
            new String[] {"authenticate(email, password) : User", "authorize(user, action) : boolean", "logout(userId) : void"});
        cls("EventController", "control",
            new String[] {"events : List<Event>", "registrations : List<EventRegistration>"},
            new String[] {"searchEvents(filter) : List<Event>", "getEventDetails(eventId) : Event", "registerStudent(student, event) : EventRegistration", "cancelRegistration(registrationId) : void", "createEvent(eventData) : Event", "updateEvent(eventId, eventData) : void"});
        cls("ClubController", "control",
            new String[] {"clubs : List<Club>", "memberships : List<ClubMembership>"},
            new String[] {"getClub(clubId) : Club", "requestMembership(student, club) : ClubMembership", "approveMembership(membershipId) : void", "postAnnouncement(clubId, message) : Announcement"});
        cls("AdminController", "control",
            new String[] {"flaggedItems : List<FlaggedContent>"},
            new String[] {"approveClub(clubId) : void", "reviewFlaggedContent(contentId) : FlaggedContent", "removeFlaggedContent(contentId) : void", "suspendClub(clubId) : void"});

        IClass user = cls("User", "entity",
            new String[] {"userId : String", "name : String", "email : String", "passwordHash : String"},
            new String[] {"login() : void", "logout() : void", "updateProfile(name, email) : void"});
        user.setAbstract(true);
        cls("Student", "entity",
            new String[] {"major : String", "graduationYear : int"},
            new String[] {"browseEvents(filter) : List<Event>", "registerForEvent(event) : EventRegistration", "cancelRegistration(registration) : void", "joinClub(club) : ClubMembership"});
        cls("ClubLeader", "entity",
            new String[] {"managedClubIds : List<String>"},
            new String[] {"createEvent(club, eventData) : Event", "updateEvent(event, eventData) : void", "approveMember(membership) : void", "postAnnouncement(club, message) : Announcement"});
        cls("Administrator", "entity",
            new String[] {"adminLevel : String"},
            new String[] {"approveClub(club) : void", "reviewFlaggedContent(content) : void", "removeContent(content) : void"});
        cls("Club", "entity",
            new String[] {"clubId : String", "name : String", "description : String", "category : String", "status : String"},
            new String[] {"addEvent(event) : void", "removeEvent(eventId) : void", "addMember(membership) : void", "postAnnouncement(announcement) : void"});
        cls("Event", "entity",
            new String[] {"eventId : String", "title : String", "description : String", "category : String", "dateTime : Date", "location : String", "capacity : int", "registrationCount : int", "fee : double", "currentState : EventState"},
            new String[] {"publish() : void", "registerStudent(student) : EventRegistration", "cancel() : void", "updateDetails(eventData) : void", "setState(state) : void", "isFull() : boolean"});
        cls("ClubMembership", "entity",
            new String[] {"membershipId : String", "studentId : String", "clubId : String", "status : String", "requestDate : Date"},
            new String[] {"approve() : void", "reject() : void", "cancel() : void"});
        cls("EventRegistration", "entity",
            new String[] {"registrationId : String", "studentId : String", "eventId : String", "registrationDate : Date", "status : String"},
            new String[] {"confirm() : void", "cancel() : void", "markAttended() : void"});
        cls("Announcement", "entity",
            new String[] {"announcementId : String", "clubId : String", "title : String", "message : String", "postedDate : Date"},
            new String[] {"publish() : void", "edit(message) : void", "archive() : void"});
        cls("FlaggedContent", "entity",
            new String[] {"contentId : String", "reporterId : String", "reason : String", "status : String", "reportedDate : Date"},
            new String[] {"markReviewed() : void", "remove() : void", "dismiss() : void"});

        iface("EventState",
            new String[] {"publish(event) : void", "registerStudent(event, student) : EventRegistration", "cancel(event) : void", "updateDetails(event, eventData) : void"});
        cls("DraftEventState", "",
            new String[] {},
            new String[] {"publish(event) : void", "registerStudent(event, student) : EventRegistration", "cancel(event) : void", "updateDetails(event, eventData) : void"});
        cls("PublishedEventState", "",
            new String[] {},
            new String[] {"publish(event) : void", "registerStudent(event, student) : EventRegistration", "cancel(event) : void", "updateDetails(event, eventData) : void"});
        cls("FullEventState", "",
            new String[] {},
            new String[] {"publish(event) : void", "registerStudent(event, student) : EventRegistration", "cancel(event) : void", "updateDetails(event, eventData) : void"});
        cls("CancelledEventState", "",
            new String[] {},
            new String[] {"publish(event) : void", "registerStudent(event, student) : EventRegistration", "cancel(event) : void", "updateDetails(event, eventData) : void"});

        iface("DashboardCommand",
            new String[] {"execute() : void", "getCommandName() : String"});
        cls("CreateEventCommand", "",
            new String[] {"leader : ClubLeader", "club : Club", "eventData : EventData", "eventController : EventController"},
            new String[] {"execute() : void", "getCommandName() : String"});
        cls("UpdateEventCommand", "",
            new String[] {"leader : ClubLeader", "event : Event", "eventData : EventData", "eventController : EventController"},
            new String[] {"execute() : void", "getCommandName() : String"});
        cls("ApproveMemberCommand", "",
            new String[] {"leader : ClubLeader", "membership : ClubMembership", "clubController : ClubController"},
            new String[] {"execute() : void", "getCommandName() : String"});
        cls("PostAnnouncementCommand", "",
            new String[] {"leader : ClubLeader", "club : Club", "message : String", "clubController : ClubController"},
            new String[] {"execute() : void", "getCommandName() : String"});
        cls("ReviewFlaggedContentCommand", "",
            new String[] {"administrator : Administrator", "flaggedContent : FlaggedContent", "decision : String", "adminController : AdminController"},
            new String[] {"execute() : void", "getCommandName() : String"});
    }

    private static void createRelationships() throws Exception {
        gen("Student", "User");
        gen("ClubLeader", "User");
        gen("Administrator", "User");

        dep("LoginPage", "SunDevilConnectFacade", "uses");
        dep("EventSearchPage", "SunDevilConnectFacade", "uses");
        dep("EventDetailsPage", "SunDevilConnectFacade", "uses");
        dep("ClubPage", "SunDevilConnectFacade", "uses");
        dep("ClubDashboardPage", "SunDevilConnectFacade", "uses");
        dep("AdminPanelPage", "SunDevilConnectFacade", "uses");

        assoc("SunDevilConnectFacade", "AuthController", "coordinates");
        assoc("SunDevilConnectFacade", "EventController", "coordinates");
        assoc("SunDevilConnectFacade", "ClubController", "coordinates");
        assoc("SunDevilConnectFacade", "AdminController", "coordinates");
        dep("SunDevilConnectFacade", "DashboardCommand", "executes");

        assoc("AuthController", "User", "authenticates");
        assoc("EventController", "Event", "manages");
        assoc("EventController", "EventRegistration", "manages");
        assoc("ClubController", "Club", "manages");
        assoc("ClubController", "ClubMembership", "manages");
        assoc("ClubController", "Announcement", "posts");
        assoc("AdminController", "Club", "approves");
        assoc("AdminController", "FlaggedContent", "reviews");

        assoc("ClubLeader", "Club", "manages");
        assoc("Club", "Event", "owns");
        assoc("Club", "Announcement", "posts");
        assoc("Club", "ClubMembership", "has");
        assoc("Student", "ClubMembership", "requests");
        assoc("Student", "EventRegistration", "makes");
        assoc("Event", "EventRegistration", "receives");
        assoc("Administrator", "FlaggedContent", "reviews");

        assoc("Event", "EventState", "currentState");
        realize("DraftEventState", "EventState");
        realize("PublishedEventState", "EventState");
        realize("FullEventState", "EventState");
        realize("CancelledEventState", "EventState");

        realize("CreateEventCommand", "DashboardCommand");
        realize("UpdateEventCommand", "DashboardCommand");
        realize("ApproveMemberCommand", "DashboardCommand");
        realize("PostAnnouncementCommand", "DashboardCommand");
        realize("ReviewFlaggedContentCommand", "DashboardCommand");

        dep("ClubDashboardPage", "DashboardCommand", "creates");
        dep("AdminPanelPage", "DashboardCommand", "creates");
        assoc("CreateEventCommand", "EventController", "uses");
        assoc("UpdateEventCommand", "EventController", "uses");
        assoc("ApproveMemberCommand", "ClubController", "uses");
        assoc("PostAnnouncementCommand", "ClubController", "uses");
        assoc("ReviewFlaggedContentCommand", "AdminController", "uses");
    }

    private static void gen(String child, String parent) throws Exception {
        generalizations.add(modelEditor.createGeneralization(classes.get(child), classes.get(parent), ""));
    }

    private static void realize(String child, String parent) throws Exception {
        realizations.add(modelEditor.createRealization(classes.get(child), classes.get(parent), ""));
    }

    private static void dep(String from, String to, String name) throws Exception {
        dependencies.add(modelEditor.createDependency(classes.get(from), classes.get(to), name));
    }

    private static void assoc(String from, String to, String name) throws Exception {
        associations.add(modelEditor.createAssociation(classes.get(from), classes.get(to), name, "", ""));
    }

    private static void createDiagram() throws Exception {
        diagramEditor.createClassDiagram(pkg, "Phase II Refined Class Diagram");

        node("LoginPage", 20, 20);
        node("EventSearchPage", 20, 180);
        node("EventDetailsPage", 20, 360);
        node("ClubPage", 20, 520);
        node("ClubDashboardPage", 20, 700);
        node("AdminPanelPage", 20, 900);

        node("SunDevilConnectFacade", 380, 320);
        node("AuthController", 780, 40);
        node("EventController", 780, 230);
        node("ClubController", 780, 440);
        node("AdminController", 780, 650);

        node("User", 1180, 20);
        node("Student", 1040, 220);
        node("ClubLeader", 1240, 220);
        node("Administrator", 1450, 220);
        node("Club", 1180, 450);
        node("Event", 1040, 700);
        node("ClubMembership", 1280, 720);
        node("EventRegistration", 1040, 960);
        node("Announcement", 1280, 950);
        node("FlaggedContent", 1500, 700);

        node("EventState", 1780, 520);
        node("DraftEventState", 1720, 700);
        node("PublishedEventState", 1950, 700);
        node("FullEventState", 1720, 900);
        node("CancelledEventState", 1950, 900);

        node("DashboardCommand", 1780, 80);
        node("CreateEventCommand", 1640, 250);
        node("UpdateEventCommand", 1860, 250);
        node("ApproveMemberCommand", 2080, 250);
        node("PostAnnouncementCommand", 1640, 420);
        node("ReviewFlaggedContentCommand", 1880, 420);

        for (IGeneralization g : generalizations) {
            link(g);
        }
        for (IRealization r : realizations) {
            link(r);
        }
        for (IDependency d : dependencies) {
            link(d);
        }
        for (IAssociation a : associations) {
            link(a);
        }
    }

    private static void node(String name, double x, double y) throws Exception {
        INodePresentation p = diagramEditor.createNodePresentation(classes.get(name), new Point2D.Double(x, y));
        p.setWidth(180.0d);
        p.setProperty("fill.color", colorFor(name));
        nodes.put(name, p);
    }

    private static String colorFor(String name) {
        if (name.endsWith("Page")) return "#E8F3FF";
        if (name.endsWith("Controller") || name.equals("SunDevilConnectFacade")) return "#FFF2CC";
        if (name.equals("EventState") || name.endsWith("EventState")) return "#EADCF8";
        if (name.equals("DashboardCommand") || name.endsWith("Command")) return "#E2F0D9";
        return "#F7F7F7";
    }

    private static void link(IGeneralization g) throws Exception {
        diagramEditor.createLinkPresentation(g, nodes.get(g.getSuperType().getName()), nodes.get(g.getSubType().getName()));
    }

    private static void link(IRealization r) throws Exception {
        diagramEditor.createLinkPresentation(r, nodes.get(r.getSupplier().getName()), nodes.get(r.getClient().getName()));
    }

    private static void link(IDependency d) throws Exception {
        diagramEditor.createLinkPresentation(d, nodes.get(d.getSupplier().getName()), nodes.get(d.getClient().getName()));
    }

    private static void link(IAssociation a) throws Exception {
        String n0 = a.getMemberEnds()[0].getType().getName();
        String n1 = a.getMemberEnds()[1].getType().getName();
        diagramEditor.createLinkPresentation(a, nodes.get(n0), nodes.get(n1));
    }
}
