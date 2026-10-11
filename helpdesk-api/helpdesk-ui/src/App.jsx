import { useEffect, useState } from "react";
import "./App.css";

function App() {
  
const [tickets, setTickets] = useState([]);
const [loading, setLoading] = useState(true);
const [error, setError] = useState("");
const [username, setUsername] = useState("");
const [password, setPassword] = useState("");
const [loggedIn, setLoggedIn] = useState(false);
const [loginError, setLoginError] = useState("");
const [description, setDescription] = useState("");
const [createError, setCreateError] = useState("");
const [creating, setCreating] = useState(false);
const [currentUser, setCurrentUser] = useState(null);
const [selectedTicketId, setSelectedTicketId] = useState(null);
const [comments, setComments] = useState([]);
const [newComment, setNewComment] = useState("");
const [commentError, setCommentError] = useState("");
const [loadingComments, setLoadingComments] = useState(false);
const [history, setHistory] = useState([]);
const [selectedHistoryTicketId, setSelectedHistoryTicketId] =useState(null);
const [historyError, setHistoryError] = useState("");
const [loadingHistory, setLoadingHistory] = useState(false);

  useEffect(() => {
    if (!loggedIn) return;
    async function loadTickets() {
      try {
        const response = await fetch(
          "http://localhost:8080/api/tickets",
          {
            credentials: "include",
          }
        );

        if (!response.ok) {
          throw new Error(
            `Unable to load tickets. HTTP ${response.status}`
          );
        }

        const data = await response.json();
        setTickets(data);
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    }

    loadTickets();
  }, [loggedIn]);


  const [title, setTitle] = useState("");
  const [priority, setPriority] = useState("MEDIUM");

  
async function createTicket(event) {
  event.preventDefault();

  if (!title.trim() || !description.trim()) {
    setCreateError("Please enter a title and description.");
    return;
  }

  if (!currentUser?.id) {
    setCreateError("User session not found. Please log in again.");
    return;
  }

  setCreating(true);
  setCreateError("");

  try {
    // Get a CSRF token before sending the POST request.
    const csrfResponse = await fetch(
      "http://localhost:8080/api/auth/csrf",
      { credentials: "include" }
    );

    if (!csrfResponse.ok) {
      throw new Error("Could not obtain CSRF token.");
    }

    const csrfData = await csrfResponse.json();

    // Send the ticket to Spring Boot.
    const response = await fetch(
      "http://localhost:8080/api/tickets",
      {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          "X-XSRF-TOKEN": csrfData.token,
        },
        body: JSON.stringify({
          title: title.trim(),
          description: description.trim(),
          priority,
          createdByUserId: currentUser.id,
        }),
      }
    );

    if (!response.ok) {
      const message = await response.text();
      throw new Error(
        `Could not create ticket (HTTP ${response.status}): ${message}`
      );
    }

    const newTicket = await response.json();

    // Display the ticket returned by the backend.
    setTickets((previousTickets) => [
      newTicket,
      ...previousTickets,
    ]);

    setTitle("");
    setDescription("");
    setPriority("MEDIUM");
  } catch (err) {
    setCreateError(err.message);
  } finally {
    setCreating(false);
  }
}


  

async function login(event) {
  event.preventDefault();
  setLoginError("");

  try {
    // Get the CSRF token
    const csrfResponse = await fetch(
      "http://localhost:8080/api/auth/csrf",
      { credentials: "include" }
    );

    if (!csrfResponse.ok) {
      throw new Error("Could not obtain CSRF token");
    }

    const csrfData = await csrfResponse.json();

    // Log in using the CSRF token
    const loginResponse = await fetch(
      "http://localhost:8080/api/auth/login",
      {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          "X-XSRF-TOKEN": csrfData.token,
        },
        body: JSON.stringify({ username, password }),
      }
    );

    if (!loginResponse.ok) {
      throw new Error(
        `Login failed: HTTP ${loginResponse.status}`
      );
    }
    const userData = await loginResponse.json();

    setCurrentUser(userData);
    setLoggedIn(true);
    setPassword("");
  } catch (err) {
    setLoginError(err.message);
  }
}

async function updateTicketStatus(ticketId, newStatus) {
  setCreateError("");

  try {
    const csrfResponse = await fetch(
      "http://localhost:8080/api/auth/csrf",
      { credentials: "include" }
    );

    if (!csrfResponse.ok) {
      throw new Error("Could not obtain CSRF token.");
    }

    const csrfData = await csrfResponse.json();

    const response = await fetch(
      `http://localhost:8080/api/tickets/${ticketId}/status?changedByUserId=${currentUser.id}`,
      {
        method: "PATCH",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          "X-XSRF-TOKEN": csrfData.token,
        },
        body: JSON.stringify({
          status: newStatus,
        }),
      }
    );

    if (!response.ok) {
      throw new Error(
        `Status update failed: HTTP ${response.status}`
      );
    }

    const updatedTicket = await response.json();

    setTickets((previousTickets) =>
      previousTickets.map((ticket) =>
        ticket.id === ticketId ? updatedTicket : ticket
      )
    );
  } catch (err) {
    setCreateError(err.message);
  }
}

async function loadComments(ticketId) {
  setSelectedTicketId(ticketId);
  setComments([]);
  setCommentError("");
  setLoadingComments(true);

  try {
    const response = await fetch(
      `http://localhost:8080/api/tickets/${ticketId}/comments`,
      {
        method: "GET",
        credentials: "include",
      }
    );

    if (!response.ok) {
      throw new Error(`Failed to load comments: ${response.status}`);
    }

    const data = await response.json();
    setComments(data);
  } catch (error) {
    setCommentError(error.message);
  } finally {
    setLoadingComments(false);
  }
}

async function addComment(event) {
  event.preventDefault();
  setCommentError("");

  if (!newComment.trim()) {
    setCommentError("Please enter a comment.");
    return;
  }

  try {
    const csrfResponse = await fetch(
      "http://localhost:8080/api/auth/csrf",
      { credentials: "include" }
    );

    if (!csrfResponse.ok) {
      throw new Error("Unable to obtain CSRF token.");
    }

    const csrfData = await csrfResponse.json();

    const response = await fetch(
      `http://localhost:8080/api/tickets/${selectedTicketId}/comments`,
      {
        method: "POST",
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
          "X-XSRF-TOKEN": csrfData.token,
        },
        body: JSON.stringify({
          commentText: newComment.trim(),
          userId: currentUser.id,
        }),
      }
    );

    if (!response.ok) {
      throw new Error(`Failed to add comment: ${response.status}`);
    }

    setNewComment("");
    await loadComments(selectedTicketId);
  } catch (error) {
    setCommentError(error.message);
  }
}
async function loadTicketHistory(ticketId) {
  setSelectedHistoryTicketId(ticketId);
  setHistory([]);
  setHistoryError("");
  setLoadingHistory(true);

  try {
    const response = await fetch(
      `http://localhost:8080/api/tickets/${ticketId}/history`,
      {
        method: "GET",
        credentials: "include",
      }
    );

    if (!response.ok) {
      throw new Error(
        `Unable to load ticket history: ${response.status}`
      );
    }

    const data = await response.json();
    setHistory(data);
  } catch (error) {
    setHistoryError(error.message);
  } finally {
    setLoadingHistory(false);
  }
}

return (
  <>
    {!loggedIn ? (
      <main className="app">
        <section className="panel">
          <h1>Helpdesk Login</h1>

          <form onSubmit={login} className="ticket-form">
            <input
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              placeholder="Username"
              autoComplete="username"
              required
            />

            <input
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Password"
              autoComplete="current-password"
              required
            />

            <button type="submit">Login</button>
          </form>

          {loginError && <p role="alert">{loginError}</p>}
        </section>
      </main>
    ) : (
      <main className="app">
        <header className="header">
        <div>
          <h1>Helpdesk</h1>
          <p>Manage your support tickets</p>
        </div>
        <span className="header-label">Support Portal</span>
      </header>

      <section className="summary">
        <div className="summary-card">
          <span>Total Tickets</span>
          <h2>{tickets.length}</h2>
        </div>

        <div className="summary-card">
          <span>Open Tickets</span>
          <h2>
            {tickets.filter((ticket) => ticket.status === "OPEN").length}
          </h2>
        </div>

        <div className="summary-card">
          <span>Resolved</span>
          <h2>
            {tickets.filter((ticket) => ticket.status === "RESOLVED").length}
          </h2>
        </div>
      </section>

      <section className="panel">
        <h2>Create a ticket</h2>
        
        <form onSubmit={createTicket} className="ticket-form">
          <input
            value={title}
            onChange={(event) => setTitle(event.target.value)}
            placeholder="Ticket title"
            aria-label="Ticket title"
            maxLength={150}
            required
          />

          <textarea
            value={description}
            onChange={(event) => setDescription(event.target.value)}
            placeholder="Describe the issue"
            aria-label="Ticket description"
            required
          />

          <select
            value={priority}
            onChange={(event) => setPriority(event.target.value)}
            aria-label="Priority"
          >
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
            <option value="CRITICAL">Critical</option>
          </select>

          <button type="submit" disabled={creating}>
            {creating ? "Creating..." : "Create ticket"}
          </button>
        </form>

        {createError && <p role="alert">{createError}</p>}

      </section>

      <section className="panel">
        <h2>Recent tickets</h2>
        {loading && <p>Loading tickets...</p>}
        {error && <p role="alert">{error}</p>}
        <div className="ticket-list">
          {tickets.map((ticket) => (
            <article className="ticket" key={ticket.id}>
              <div>
                <strong>#{ticket.id}</strong>
                <h3>{ticket.title}</h3>
                <span className="priority">
                  Priority: {ticket.priority}
                </span>
              </div>

              <select
                className={`status-select ${ticket.status.toLowerCase()}`}
                value={ticket.status}
                onChange={(event) =>
                  updateTicketStatus(ticket.id, event.target.value)
                }
                aria-label={`Status for ticket ${ticket.id}`}
              >
                <option value="OPEN">Open</option>
                <option value="IN_PROGRESS">In Progress</option>
                <option value="RESOLVED">Resolved</option>
              </select>
              <button
                type="button"
                onClick={() => loadComments(ticket.id)}
              >
                View Comments
              </button>
              <button
                type="button"
                onClick={() => loadTicketHistory(ticket.id)}
              >
                View History
              </button>
            </article>
          ))}
        </div>
      </section>
      {selectedTicketId !== null && (
        <section className="panel comments-panel">
          <h2>Comments — Ticket #{selectedTicketId}</h2>

          <button
            type="button"
            onClick={() => {
              setSelectedTicketId(null);
              setComments([]);
              setCommentError("");
            }}
          >
            Close
          </button>

          {loadingComments && <p>Loading comments...</p>}

          {commentError && (
            <p role="alert" className="error-message">
              {commentError}
            </p>
          )}

          {!loadingComments && comments.length === 0 && (
            <p>No comments yet.</p>
          )}

          <div className="comments-list">
            {comments.map((comment) => (
              <article key={comment.id} className="comment-item">
                <p>{comment.commentText}</p>
                <small>
                  {comment.createdAt
                    ? new Date(comment.createdAt).toLocaleString()
                    : ""}
                </small>
              </article>
            ))}
          </div>

          <form onSubmit={addComment} className="ticket-form">
            <textarea
              value={newComment}
              onChange={(event) => setNewComment(event.target.value)}
              placeholder="Write a comment..."
              rows={3}
              required
            />

            <button type="submit">Add Comment</button>
          </form>
        </section>
        
      )}
      {selectedHistoryTicketId !== null && (
        <section className="panel history-panel">
          <div className="history-header">
            <h2>Ticket History #{selectedHistoryTicketId}</h2>

            <button
              type="button"
              onClick={() => {
                setSelectedHistoryTicketId(null);
                setHistory([]);
                setHistoryError("");
              }}
            >
              Close
            </button>
          </div>

          {loadingHistory && <p>Loading history...</p>}

          {historyError && (
            <p role="alert" className="error-message">
              {historyError}
            </p>
          )}

          {!loadingHistory &&
            !historyError &&
            history.length === 0 && (
              <p>No history events found.</p>
            )}

          <div className="timeline">
            {history.map((event) => (
              <article key={event.id} className="timeline-item">
                <div className="timeline-dot" />

                <div className="timeline-content">
                  <h3>
                    {event.action || event.actionType || "Ticket updated"}
                  </h3>

                  {event.oldStatus && (
                    <p>Previous status: {event.oldStatus}</p>
                  )}

                  {event.newStatus && (
                    <p>New status: {event.newStatus}</p>
                  )}

                  {event.changedByUserId && (
                    <p>Changed by user ID: {event.changedByUserId}</p>
                  )}

                  {event.createdAt && (
                    <small>
                      {new Date(event.createdAt).toLocaleString()}
                    </small>
                  )}
                </div>
              </article>
            ))}
          </div>
        </section>
      )}
      </main>
    )}
  </>
);

}

export default App;
