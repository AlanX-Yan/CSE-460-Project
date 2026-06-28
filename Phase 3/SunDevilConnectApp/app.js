class DraftEventState {
  get name() {
    return "Draft";
  }

  registerStudent() {
    return { ok: false, message: "Draft events are not open for registration." };
  }

  publish(event) {
    event.state = new PublishedEventState();
    return { ok: true, message: `${event.title} was published.` };
  }

  cancel(event) {
    event.state = new CancelledEventState();
    return { ok: true, message: `${event.title} was cancelled.` };
  }
}

class PublishedEventState {
  get name() {
    return "Published";
  }

  registerStudent(event, studentId) {
    if (event.registrations.includes(studentId)) {
      return { ok: false, message: "You are already registered for this event." };
    }
    if (event.registrations.length >= event.capacity) {
      event.state = new FullEventState();
      return { ok: false, message: "This event is full." };
    }
    event.registrations.push(studentId);
    if (event.registrations.length >= event.capacity) {
      event.state = new FullEventState();
    }
    return { ok: true, message: `Registered for ${event.title}.` };
  }

  publish() {
    return { ok: false, message: "This event is already published." };
  }

  cancel(event) {
    event.state = new CancelledEventState();
    return { ok: true, message: `${event.title} was cancelled.` };
  }
}

class FullEventState {
  get name() {
    return "Full";
  }

  registerStudent() {
    return { ok: false, message: "This event is at capacity." };
  }

  publish() {
    return { ok: false, message: "This event is already open and full." };
  }

  cancel(event) {
    event.state = new CancelledEventState();
    return { ok: true, message: `${event.title} was cancelled.` };
  }
}

class CancelledEventState {
  get name() {
    return "Cancelled";
  }

  registerStudent() {
    return { ok: false, message: "Cancelled events do not accept registrations." };
  }

  publish() {
    return { ok: false, message: "Cancelled events cannot be published again." };
  }

  cancel() {
    return { ok: false, message: "This event is already cancelled." };
  }
}

class EventEntity {
  constructor(data) {
    Object.assign(this, data);
    this.registrations = data.registrations || [];
    this.state = data.state || new PublishedEventState();
  }

  registerStudent(studentId) {
    return this.state.registerStudent(this, studentId);
  }

  cancelRegistration(studentId) {
    const index = this.registrations.indexOf(studentId);
    if (index === -1) {
      return { ok: false, message: "No registration found for this event." };
    }
    this.registrations.splice(index, 1);
    if (this.state instanceof FullEventState) {
      this.state = new PublishedEventState();
    }
    return { ok: true, message: `Cancelled registration for ${this.title}.` };
  }

  publish() {
    return this.state.publish(this);
  }

  cancel() {
    return this.state.cancel(this);
  }
}

class AuthController {
  constructor(store) {
    this.store = store;
  }

  getCurrentUser(role) {
    return this.store.users.find((user) => user.role === role);
  }
}

class EventController {
  constructor(store) {
    this.store = store;
  }

  searchEvents({ keyword, category, cost, sortBy }) {
    const normalized = keyword.trim().toLowerCase();
    let events = this.store.events.filter((event) => {
      const matchesText =
        !normalized ||
        event.title.toLowerCase().includes(normalized) ||
        event.description.toLowerCase().includes(normalized) ||
        event.clubName.toLowerCase().includes(normalized);
      const matchesCategory = category === "all" || event.category === category;
      const matchesCost = cost === "all" || (cost === "free" ? event.fee === 0 : event.fee > 0);
      return matchesText && matchesCategory && matchesCost;
    });

    events = events.sort((a, b) => {
      if (sortBy === "popular") return b.popularity - a.popularity;
      return new Date(a.date) - new Date(b.date);
    });

    return events;
  }

  createEvent(data) {
    const event = new EventEntity({
      id: `event-${Date.now()}`,
      clubId: "club-code",
      clubName: "Software Developers Association",
      popularity: 0,
      registrations: [],
      state: new PublishedEventState(),
      ...data,
    });
    this.store.events.unshift(event);
    return event;
  }

  register(studentId, eventId) {
    const event = this.store.events.find((item) => item.id === eventId);
    return event.registerStudent(studentId);
  }

  cancelRegistration(studentId, eventId) {
    const event = this.store.events.find((item) => item.id === eventId);
    return event.cancelRegistration(studentId);
  }
}

class ClubController {
  constructor(store) {
    this.store = store;
  }

  requestMembership(studentId, clubId) {
    const existing = this.store.memberships.find(
      (item) => item.studentId === studentId && item.clubId === clubId
    );
    if (existing) {
      return { ok: false, message: "A membership request already exists for this club." };
    }
    this.store.memberships.push({
      id: `membership-${Date.now()}`,
      studentId,
      studentName: "Xu Yan",
      clubId,
      status: "Pending",
      requestedAt: new Date().toISOString().slice(0, 10),
    });
    return { ok: true, message: "Membership request submitted." };
  }

  approveMembership(membershipId) {
    const membership = this.store.memberships.find((item) => item.id === membershipId);
    membership.status = "Approved";
    return { ok: true, message: `${membership.studentName} was approved.` };
  }

  postAnnouncement(message) {
    const announcement = {
      id: `announcement-${Date.now()}`,
      clubId: "club-code",
      clubName: "Software Developers Association",
      message,
      postedAt: new Date().toLocaleString(),
    };
    this.store.announcements.unshift(announcement);
    return announcement;
  }
}

class AdminController {
  constructor(store) {
    this.store = store;
  }

  approveClub(clubId) {
    const club = this.store.clubs.find((item) => item.id === clubId);
    club.status = "Active";
    return { ok: true, message: `${club.name} was approved.` };
  }

  reviewFlaggedContent(contentId, decision) {
    const item = this.store.flaggedContent.find((content) => content.id === contentId);
    item.status = decision;
    return { ok: true, message: `Flagged content marked as ${decision}.` };
  }
}

class DashboardCommand {
  execute() {
    throw new Error("Commands must implement execute().");
  }

  getCommandName() {
    return this.constructor.name;
  }
}

class CreateEventCommand extends DashboardCommand {
  constructor(eventController, payload) {
    super();
    this.eventController = eventController;
    this.payload = payload;
  }

  execute() {
    const event = this.eventController.createEvent(this.payload);
    return { ok: true, message: `Created event: ${event.title}` };
  }
}

class ApproveMemberCommand extends DashboardCommand {
  constructor(clubController, membershipId) {
    super();
    this.clubController = clubController;
    this.membershipId = membershipId;
  }

  execute() {
    return this.clubController.approveMembership(this.membershipId);
  }
}

class PostAnnouncementCommand extends DashboardCommand {
  constructor(clubController, message) {
    super();
    this.clubController = clubController;
    this.message = message;
  }

  execute() {
    const announcement = this.clubController.postAnnouncement(this.message);
    return { ok: true, message: `Posted announcement for ${announcement.clubName}.` };
  }
}

class ReviewFlaggedContentCommand extends DashboardCommand {
  constructor(adminController, contentId, decision) {
    super();
    this.adminController = adminController;
    this.contentId = contentId;
    this.decision = decision;
  }

  execute() {
    return this.adminController.reviewFlaggedContent(this.contentId, this.decision);
  }
}

class SunDevilConnectFacade {
  constructor(store) {
    this.store = store;
    this.authController = new AuthController(store);
    this.eventController = new EventController(store);
    this.clubController = new ClubController(store);
    this.adminController = new AdminController(store);
  }

  getCurrentUser(role) {
    return this.authController.getCurrentUser(role);
  }

  browseEvents(filters) {
    return this.eventController.searchEvents(filters);
  }

  registerForEvent(studentId, eventId) {
    return this.eventController.register(studentId, eventId);
  }

  cancelEventRegistration(studentId, eventId) {
    return this.eventController.cancelRegistration(studentId, eventId);
  }

  joinClub(studentId, clubId) {
    return this.clubController.requestMembership(studentId, clubId);
  }

  executeCommand(command) {
    const result = command.execute();
    this.store.commandLog.unshift({
      name: command.getCommandName(),
      result: result.message,
      at: new Date().toLocaleString(),
    });
    return result;
  }
}

const store = {
  users: [
    { id: "student-xu", name: "Xu Yan", role: "student" },
    { id: "leader-1", name: "Taylor Kim", role: "leader" },
    { id: "admin-1", name: "Jordan Lee", role: "admin" },
  ],
  events: [
    new EventEntity({
      id: "event-ai",
      title: "AI Project Night",
      clubId: "club-code",
      clubName: "Software Developers Association",
      category: "Tech",
      date: "2026-07-02",
      location: "Memorial Union 230",
      capacity: 35,
      fee: 0,
      popularity: 92,
      description: "A working session for students building portfolio AI and software projects.",
      registrations: ["student-demo"],
    }),
    new EventEntity({
      id: "event-career",
      title: "Resume Review Sprint",
      clubId: "club-career",
      clubName: "Career Builders Club",
      category: "Career",
      date: "2026-07-05",
      location: "Student Pavilion",
      capacity: 20,
      fee: 0,
      popularity: 85,
      description: "Peer and mentor resume review with quick feedback rounds.",
    }),
    new EventEntity({
      id: "event-music",
      title: "Open Mic Night",
      clubId: "club-music",
      clubName: "Tempe Music Collective",
      category: "Music",
      date: "2026-07-08",
      location: "Hayden Lawn",
      capacity: 60,
      fee: 5,
      popularity: 77,
      description: "A casual evening for performers, poets, and student bands.",
    }),
    new EventEntity({
      id: "event-soccer",
      title: "Sunset Soccer Social",
      clubId: "club-sports",
      clubName: "Campus Recreation Club",
      category: "Sports",
      date: "2026-07-10",
      location: "SDFC Fields",
      capacity: 18,
      fee: 0,
      popularity: 70,
      description: "Drop-in soccer followed by snacks and club signups.",
    }),
  ],
  clubs: [
    {
      id: "club-code",
      name: "Software Developers Association",
      category: "Tech",
      status: "Active",
      description: "Workshops, hack nights, and student-led software projects.",
    },
    {
      id: "club-career",
      name: "Career Builders Club",
      category: "Career",
      status: "Pending",
      description: "Interview practice, resume reviews, and career mentorship.",
    },
    {
      id: "club-music",
      name: "Tempe Music Collective",
      category: "Music",
      status: "Active",
      description: "Student performances, open mic events, and music meetups.",
    },
  ],
  memberships: [
    {
      id: "membership-1",
      studentId: "student-maya",
      studentName: "Maya Patel",
      clubId: "club-code",
      status: "Pending",
      requestedAt: "2026-06-28",
    },
  ],
  announcements: [
    {
      id: "announcement-1",
      clubId: "club-code",
      clubName: "Software Developers Association",
      message: "Bring your laptop for AI Project Night.",
      postedAt: "2026-06-28 6:15 PM",
    },
  ],
  flaggedContent: [
    {
      id: "flag-1",
      source: "Open Mic Night comment",
      reason: "Inappropriate language reported by a student",
      status: "Pending",
    },
    {
      id: "flag-2",
      source: "Club announcement",
      reason: "Potentially incorrect event fee information",
      status: "Pending",
    },
  ],
  commandLog: [],
};

const facade = new SunDevilConnectFacade(store);
const elements = {
  roleSelect: document.querySelector("#roleSelect"),
  tabs: document.querySelectorAll(".tab"),
  views: document.querySelectorAll(".view"),
  searchInput: document.querySelector("#searchInput"),
  categoryFilter: document.querySelector("#categoryFilter"),
  costFilter: document.querySelector("#costFilter"),
  sortFilter: document.querySelector("#sortFilter"),
  eventList: document.querySelector("#eventList"),
  eventCount: document.querySelector("#eventCount"),
  clubList: document.querySelector("#clubList"),
  studentActivity: document.querySelector("#studentActivity"),
  createEventForm: document.querySelector("#createEventForm"),
  membershipQueue: document.querySelector("#membershipQueue"),
  announcementForm: document.querySelector("#announcementForm"),
  announcementList: document.querySelector("#announcementList"),
  clubApprovals: document.querySelector("#clubApprovals"),
  flaggedContent: document.querySelector("#flaggedContent"),
  toast: document.querySelector("#toast"),
};

function showToast(message) {
  elements.toast.textContent = message;
  elements.toast.classList.add("is-visible");
  window.setTimeout(() => elements.toast.classList.remove("is-visible"), 2400);
}

function formatFee(fee) {
  return fee === 0 ? "Free" : `$${fee}`;
}

function renderEvents() {
  const events = facade.browseEvents({
    keyword: elements.searchInput.value,
    category: elements.categoryFilter.value,
    cost: elements.costFilter.value,
    sortBy: elements.sortFilter.value,
  });
  const student = facade.getCurrentUser("student");
  elements.eventCount.textContent = `${events.length} events`;
  elements.eventList.innerHTML = events
    .map((event) => {
      const registered = event.registrations.includes(student.id);
      return `
        <article class="card">
          <div class="meta">
            <span class="tag">${event.category}</span>
            <span class="status">${event.state.name}</span>
          </div>
          <h4>${event.title}</h4>
          <p>${event.description}</p>
          <div class="meta">
            <span>${event.date}</span>
            <span>${event.location}</span>
            <span>${formatFee(event.fee)}</span>
            <span>${event.registrations.length}/${event.capacity} registered</span>
          </div>
          <div class="actions">
            <button class="${registered ? "danger" : "primary"}" data-action="${registered ? "cancel" : "register"}" data-event-id="${event.id}">
              ${registered ? "Cancel registration" : "Register"}
            </button>
          </div>
        </article>
      `;
    })
    .join("");
}

function renderClubs() {
  const student = facade.getCurrentUser("student");
  elements.clubList.innerHTML = store.clubs
    .map((club) => {
      const membership = store.memberships.find(
        (item) => item.studentId === student.id && item.clubId === club.id
      );
      return `
        <article class="club-row">
          <div class="meta">
            <span class="tag">${club.category}</span>
            <span class="status">${club.status}</span>
          </div>
          <h4>${club.name}</h4>
          <p>${club.description}</p>
          <div class="actions">
            <button class="secondary" data-action="join-club" data-club-id="${club.id}" ${membership ? "disabled" : ""}>
              ${membership ? membership.status : "Join club"}
            </button>
          </div>
        </article>
      `;
    })
    .join("");
}

function renderStudentActivity() {
  const student = facade.getCurrentUser("student");
  const registrations = store.events.filter((event) => event.registrations.includes(student.id));
  const memberships = store.memberships.filter((membership) => membership.studentId === student.id);
  const items = [
    ...registrations.map((event) => `Registered: ${event.title}`),
    ...memberships.map((membership) => {
      const club = store.clubs.find((item) => item.id === membership.clubId);
      return `${membership.status} membership: ${club.name}`;
    }),
  ];

  elements.studentActivity.innerHTML = items.length
    ? items.map((item) => `<div class="activity-item">${item}</div>`).join("")
    : `<p class="empty">No activity yet.</p>`;
}

function renderLeaderDashboard() {
  const pending = store.memberships.filter((membership) => membership.status === "Pending");
  elements.membershipQueue.innerHTML = pending.length
    ? pending
        .map(
          (membership) => `
            <article class="queue-row">
              <strong>${membership.studentName}</strong>
              <span>Requested membership on ${membership.requestedAt}</span>
              <button class="primary" data-action="approve-member" data-membership-id="${membership.id}">Approve</button>
            </article>
          `
        )
        .join("")
    : `<p class="empty">No pending membership requests.</p>`;

  elements.announcementList.innerHTML = store.announcements
    .map((item) => `<div class="activity-item">${item.postedAt}: ${item.message}</div>`)
    .join("");
}

function renderAdminDashboard() {
  const pendingClubs = store.clubs.filter((club) => club.status === "Pending");
  elements.clubApprovals.innerHTML = pendingClubs.length
    ? pendingClubs
        .map(
          (club) => `
            <article class="queue-row">
              <strong>${club.name}</strong>
              <span>${club.description}</span>
              <button class="primary" data-action="approve-club" data-club-id="${club.id}">Approve club</button>
            </article>
          `
        )
        .join("")
    : `<p class="empty">No clubs waiting for approval.</p>`;

  elements.flaggedContent.innerHTML = store.flaggedContent
    .map(
      (item) => `
        <article class="queue-row">
          <div class="meta">
            <span class="status">${item.status}</span>
          </div>
          <strong>${item.source}</strong>
          <span>${item.reason}</span>
          <div class="actions">
            <button class="secondary" data-action="dismiss-flag" data-content-id="${item.id}">Dismiss</button>
            <button class="danger" data-action="remove-flag" data-content-id="${item.id}">Remove</button>
          </div>
        </article>
      `
    )
    .join("");
}

function renderAll() {
  renderEvents();
  renderClubs();
  renderStudentActivity();
  renderLeaderDashboard();
  renderAdminDashboard();
}

function switchView(viewId) {
  elements.tabs.forEach((tab) => tab.classList.toggle("is-active", tab.dataset.view === viewId));
  elements.views.forEach((view) => view.classList.toggle("is-active", view.id === viewId));
}

elements.tabs.forEach((tab) => {
  tab.addEventListener("click", () => switchView(tab.dataset.view));
});

elements.roleSelect.addEventListener("change", (event) => {
  const roleToView = {
    student: "studentView",
    leader: "leaderView",
    admin: "adminView",
  };
  switchView(roleToView[event.target.value]);
});

[elements.searchInput, elements.categoryFilter, elements.costFilter, elements.sortFilter].forEach((element) => {
  element.addEventListener("input", renderEvents);
});

document.body.addEventListener("click", (event) => {
  const button = event.target.closest("button[data-action]");
  if (!button) return;

  const student = facade.getCurrentUser("student");
  let result;

  if (button.dataset.action === "register") {
    result = facade.registerForEvent(student.id, button.dataset.eventId);
  }
  if (button.dataset.action === "cancel") {
    result = facade.cancelEventRegistration(student.id, button.dataset.eventId);
  }
  if (button.dataset.action === "join-club") {
    result = facade.joinClub(student.id, button.dataset.clubId);
  }
  if (button.dataset.action === "approve-member") {
    result = facade.executeCommand(
      new ApproveMemberCommand(facade.clubController, button.dataset.membershipId)
    );
  }
  if (button.dataset.action === "approve-club") {
    result = facade.adminController.approveClub(button.dataset.clubId);
  }
  if (button.dataset.action === "dismiss-flag" || button.dataset.action === "remove-flag") {
    const decision = button.dataset.action === "dismiss-flag" ? "Dismissed" : "Removed";
    result = facade.executeCommand(
      new ReviewFlaggedContentCommand(facade.adminController, button.dataset.contentId, decision)
    );
  }

  if (result) {
    showToast(result.message);
    renderAll();
  }
});

elements.createEventForm.addEventListener("submit", (event) => {
  event.preventDefault();
  const formData = new FormData(event.target);
  const payload = {
    title: formData.get("title"),
    category: formData.get("category"),
    date: formData.get("date"),
    location: formData.get("location"),
    capacity: Number(formData.get("capacity")),
    fee: Number(formData.get("fee")),
    description: formData.get("description"),
  };
  const result = facade.executeCommand(new CreateEventCommand(facade.eventController, payload));
  showToast(result.message);
  event.target.reset();
  renderAll();
});

elements.announcementForm.addEventListener("submit", (event) => {
  event.preventDefault();
  const formData = new FormData(event.target);
  const result = facade.executeCommand(
    new PostAnnouncementCommand(facade.clubController, formData.get("message"))
  );
  showToast(result.message);
  event.target.reset();
  renderAll();
});

renderAll();
