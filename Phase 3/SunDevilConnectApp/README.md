# SunDevil Connect - Phase III Implementation

This folder contains the Phase III implementation for SunDevil Connect.

## How to Run

Open `index.html` in a browser.

If the browser blocks local files, run a simple local server from this folder:

```bash
python3 -m http.server 8765
```

Then open:

```text
http://127.0.0.1:8765/index.html
```

## Implemented Features

### Phase III Part I

- Student event discovery.
- Search, category, cost, and sort filters.
- Event registration and cancellation.
- Club browsing and membership request.
- Role-aware UI structure.
- Layered client-side architecture with `SunDevilConnectFacade`.

### Phase III Part II

- Club Leader dashboard.
- Create event workflow.
- Approve membership request workflow.
- Post announcement workflow.
- Admin panel.
- Approve pending club workflow.
- Review flagged content workflow.

## Design Patterns Reflected in Code

- Facade Pattern: `SunDevilConnectFacade`.
- State Pattern: `DraftEventState`, `PublishedEventState`, `FullEventState`, `CancelledEventState`.
- Command Pattern: `CreateEventCommand`, `ApproveMemberCommand`, `PostAnnouncementCommand`, `ReviewFlaggedContentCommand`.

## Main Files

- `index.html`: App structure.
- `styles.css`: Layout and visual design.
- `app.js`: Controllers, entities, design patterns, and UI behavior.
- `assets/campus-events-banner.png`: Local visual asset used in the app.
