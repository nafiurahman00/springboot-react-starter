import { useEffect, useState } from 'react';
import { api } from '../api/client.js';

// Demonstrates the full CRUD cycle against the protected /api/tasks endpoints.
export default function TasksPage() {
  const [tasks, setTasks] = useState([]);
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(true);

  // Load tasks once when the page mounts.
  useEffect(() => {
    loadTasks();
  }, []);

  async function loadTasks() {
    setLoading(true);
    setError('');
    try {
      setTasks(await api('/api/tasks'));
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  async function addTask(e) {
    e.preventDefault();
    if (!title.trim()) return;
    try {
      const created = await api('/api/tasks', {
        method: 'POST',
        body: { title, description, completed: false },
      });
      setTasks([created, ...tasks]);
      setTitle('');
      setDescription('');
    } catch (err) {
      setError(err.message);
    }
  }

  async function toggleComplete(task) {
    try {
      const updated = await api(`/api/tasks/${task.id}`, {
        method: 'PUT',
        body: {
          title: task.title,
          description: task.description,
          completed: !task.completed,
        },
      });
      setTasks(tasks.map((t) => (t.id === updated.id ? updated : t)));
    } catch (err) {
      setError(err.message);
    }
  }

  async function deleteTask(id) {
    try {
      await api(`/api/tasks/${id}`, { method: 'DELETE' });
      setTasks(tasks.filter((t) => t.id !== id));
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div>
      <h1>My Tasks</h1>

      <form className="card" onSubmit={addTask}>
        <h2>Add a task</h2>
        <input
          placeholder="Title"
          value={title}
          onChange={(e) => setTitle(e.target.value)}
          required
        />
        <textarea
          placeholder="Description (optional)"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
          rows={2}
        />
        <button className="btn" type="submit">Add</button>
      </form>

      {error && <p className="error">{error}</p>}

      {loading ? (
        <p>Loading tasks…</p>
      ) : tasks.length === 0 ? (
        <p className="muted">No tasks yet. Add your first one above.</p>
      ) : (
        <ul className="task-list">
          {tasks.map((task) => (
            <li key={task.id} className={`task ${task.completed ? 'done' : ''}`}>
              <label className="task-main">
                <input
                  type="checkbox"
                  checked={task.completed}
                  onChange={() => toggleComplete(task)}
                />
                <span>
                  <strong>{task.title}</strong>
                  {task.description && <small> — {task.description}</small>}
                </span>
              </label>
              <button className="btn btn-danger" onClick={() => deleteTask(task.id)}>
                Delete
              </button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
