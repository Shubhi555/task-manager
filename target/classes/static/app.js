 const API = '';

function getToken() { return localStorage.getItem('token'); }
function getUser() { return JSON.parse(localStorage.getItem('user') || '{}'); }

async function apiCall(method, url, body) {
    const headers = { 'Content-Type': 'application/json' };
    if (getToken()) headers['Authorization'] = 'Bearer ' + getToken();
    const res = await fetch(API + url, {
        method,
        headers,
        body: body ? JSON.stringify(body) : undefined
    });
    return res.json();
}

// ===================== AUTH =====================

function showTab(tab) {
    document.getElementById('login-form').style.display = tab === 'login' ? 'block' : 'none';
    document.getElementById('signup-form').style.display = tab === 'signup' ? 'block' : 'none';
    document.querySelectorAll('.tab-btn').forEach((b, i) => {
        b.classList.toggle('active', (i === 0 && tab === 'login') || (i === 1 && tab === 'signup'));
    });
}

async function login() {
    const email = document.getElementById('login-email').value;
    const password = document.getElementById('login-password').value;
    const data = await apiCall('POST', '/api/auth/login', { email, password });
    if (data.token) {
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify(data));
        window.location.href = 'dashboard.html';
    } else {
        document.getElementById('auth-error').textContent = data.error || 'Login failed';
    }
}

async function signup() {
    const name = document.getElementById('signup-name').value;
    const email = document.getElementById('signup-email').value;
    const password = document.getElementById('signup-password').value;
    const role = document.getElementById('signup-role').value;
    const data = await apiCall('POST', '/api/auth/signup', { name, email, password, role });
    if (data.token) {
        localStorage.setItem('token', data.token);
        localStorage.setItem('user', JSON.stringify(data));
        window.location.href = 'dashboard.html';
    } else {
        document.getElementById('auth-error').textContent = data.error || 'Signup failed';
    }
}

function logout() {
    localStorage.clear();
    window.location.href = 'index.html';
}

// ===================== DASHBOARD INIT =====================

window.onload = function () {
    if (window.location.pathname.includes('dashboard')) {
        if (!getToken()) { window.location.href = 'index.html'; return; }
        const user = getUser();
        document.getElementById('user-name').textContent = user.name || '';
        document.getElementById('user-role').textContent = user.role || '';
        loadDashboard();
        loadProjects();
        loadMyTasks();
    }
};

// ===================== SECTIONS =====================

function showSection(name) {
    ['dashboard', 'projects', 'mytasks'].forEach(s => {
        document.getElementById('section-' + s).style.display = s === name ? 'block' : 'none';
    });
    document.querySelectorAll('.sidebar-btn').forEach((b, i) => {
        b.classList.toggle('active', b.textContent.toLowerCase().includes(name.substring(0, 4)));
    });
}

// ===================== DASHBOARD STATS =====================

async function loadDashboard() {
    const tasks = await apiCall('GET', '/api/tasks/my');
    const overdue = await apiCall('GET', '/api/tasks/overdue');
    if (Array.isArray(tasks)) {
        document.getElementById('stat-total').textContent = tasks.length;
        document.getElementById('stat-todo').textContent = tasks.filter(t => t.status === 'TODO').length;
        document.getElementById('stat-progress').textContent = tasks.filter(t => t.status === 'IN_PROGRESS').length;
        document.getElementById('stat-done').textContent = tasks.filter(t => t.status === 'DONE').length;
    }
    if (Array.isArray(overdue)) {
        document.getElementById('stat-overdue').textContent = overdue.length;
    }
}

// ===================== PROJECTS =====================

async function loadProjects() {
    const projects = await apiCall('GET', '/api/projects');
    const list = document.getElementById('projects-list');
    if (!list || !Array.isArray(projects)) return;
    list.innerHTML = '';
    if (projects.length === 0) {
        list.innerHTML = '<p style="color:#888">No projects yet. Create one!</p>';
        return;
    }
    projects.forEach(p => {
        list.innerHTML += `
        <div class="project-card">
            <h3>${p.name}</h3>
            <p>${p.description || 'No description'}</p>
            <div class="project-actions">
                <button class="btn-view" onclick="viewProject(${p.id}, '${p.name}')">View Tasks</button>
                <button class="btn-view" style="background:#9b59b6" onclick="showAddMember(${p.id})">Add Member</button>
                <button class="btn-delete" onclick="deleteProject(${p.id})">Delete</button>
            </div>
            <div id="addmember-${p.id}" style="display:none;margin-top:10px">
                <input type="number" id="memberid-${p.id}" placeholder="User ID to add" style="width:180px;display:inline-block;margin-right:8px"/>
                <button class="btn-view" onclick="addMember(${p.id})">Add</button>
            </div>
            <div id="tasks-${p.id}" style="margin-top:16px;display:none">
                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:10px">
                    <strong>Tasks in ${p.name}</strong>
                    <button class="btn-view" onclick="openTaskModal(${p.id})">+ Add Task</button>
                </div>
                <div id="tasklist-${p.id}"></div>
            </div>
        </div>`;
    });
}

function showCreateProject() {
    document.getElementById('create-project-form').style.display = 'block';
}
function hideCreateProject() {
    document.getElementById('create-project-form').style.display = 'none';
}

async function createProject() {
    const name = document.getElementById('project-name').value;
    const description = document.getElementById('project-desc').value;
    if (!name) { alert('Project name is required'); return; }
    const data = await apiCall('POST', '/api/projects', { name, description });
    if (data.id) {
        hideCreateProject();
        document.getElementById('project-name').value = '';
        document.getElementById('project-desc').value = '';
        loadProjects();
    } else {
        alert(data.error || 'Failed to create project');
    }
}

async function deleteProject(id) {
    if (!confirm('Delete this project?')) return;
    await apiCall('DELETE', '/api/projects/' + id);
    loadProjects();
}

async function viewProject(id, name) {
    const taskDiv = document.getElementById('tasks-' + id);
    if (taskDiv.style.display === 'block') { taskDiv.style.display = 'none'; return; }
    taskDiv.style.display = 'block';
    const tasks = await apiCall('GET', '/api/tasks/project/' + id);
    const list = document.getElementById('tasklist-' + id);
    if (!Array.isArray(tasks) || tasks.length === 0) {
        list.innerHTML = '<p style="color:#888;font-size:13px">No tasks yet.</p>';
        return;
    }
    list.innerHTML = tasks.map(t => `
        <div class="task-card ${t.priority}">
            <div class="task-info">
                <h4>${t.title}</h4>
                <p>${t.description || ''} | Priority: ${t.priority} | Due: ${t.dueDate ? t.dueDate.substring(0,10) : 'None'}</p>
            </div>
            <div class="task-actions">
                <span class="status-badge status-${t.status}">${t.status.replace('_',' ')}</span>
                <select class="status-select" onchange="updateStatus(${t.id}, this.value)">
                    <option value="TODO" ${t.status==='TODO'?'selected':''}>To Do</option>
                    <option value="IN_PROGRESS" ${t.status==='IN_PROGRESS'?'selected':''}>In Progress</option>
                    <option value="DONE" ${t.status==='DONE'?'selected':''}>Done</option>
                </select>
                <button class="btn-delete" onclick="deleteTask(${t.id}, ${id}, '${name}')">Del</button>
            </div>
        </div>`).join('');
}

function showAddMember(id) {
    const div = document.getElementById('addmember-' + id);
    div.style.display = div.style.display === 'none' ? 'block' : 'none';
}

async function addMember(projectId) {
    const userId = document.getElementById('memberid-' + projectId).value;
    if (!userId) { alert('Enter a user ID'); return; }
    const data = await apiCall('POST', '/api/projects/' + projectId + '/members', { userId: parseInt(userId) });
    if (data.id) { alert('Member added!'); }
    else alert(data.error || 'Failed');
}

// ===================== TASKS =====================

let currentProjectId = null;

function openTaskModal(projectId) {
    currentProjectId = projectId;
    document.getElementById('task-project-id').value = projectId;
    document.getElementById('task-modal').style.display = 'flex';
}

function closeTaskModal() {
    document.getElementById('task-modal').style.display = 'none';
}

async function createTask() {
    const title = document.getElementById('task-title').value;
    const description = document.getElementById('task-desc').value;
    const priority = document.getElementById('task-priority').value;
    const dueDate = document.getElementById('task-due').value;
    const assigneeId = document.getElementById('task-assignee').value;
    const projectId = document.getElementById('task-project-id').value;
    if (!title) { alert('Task title is required'); return; }
    const body = {
        title, description, priority, projectId: parseInt(projectId),
        dueDate: dueDate ? dueDate.replace('T', 'T').padEnd(19, ':00') : null,
        assignedToId: assigneeId ? parseInt(assigneeId) : null
    };
    const data = await apiCall('POST', '/api/tasks', body);
    if (data.id) {
        closeTaskModal();
        document.getElementById('task-title').value = '';
        document.getElementById('task-desc').value = '';
        document.getElementById('task-due').value = '';
        document.getElementById('task-assignee').value = '';
        viewProject(currentProjectId, '');
        loadDashboard();
    } else {
        alert(data.error || 'Failed to create task');
    }
}

async function updateStatus(taskId, status) {
    await apiCall('PUT', '/api/tasks/' + taskId + '/status', { status });
    loadDashboard();
}

async function deleteTask(taskId, projectId, projectName) {
    if (!confirm('Delete this task?')) return;
    await apiCall('DELETE', '/api/tasks/' + taskId);
    viewProject(projectId, projectName);
    loadDashboard();
}

// ===================== MY TASKS =====================

async function loadMyTasks() {
    const tasks = await apiCall('GET', '/api/tasks/my');
    const list = document.getElementById('mytasks-list');
    if (!list || !Array.isArray(tasks)) return;
    if (tasks.length === 0) {
        list.innerHTML = '<p style="color:#888">No tasks assigned to you.</p>';
        return;
    }
    list.innerHTML = tasks.map(t => `
        <div class="task-card ${t.priority}">
            <div class="task-info">
                <h4>${t.title}</h4>
                <p>${t.description || ''} | Priority: ${t.priority} | Due: ${t.dueDate ? t.dueDate.substring(0,10) : 'None'}</p>
            </div>
            <div class="task-actions">
                <span class="status-badge status-${t.status}">${t.status.replace('_',' ')}</span>
                <select class="status-select" onchange="updateStatus(${t.id}, this.value)">
                    <option value="TODO" ${t.status==='TODO'?'selected':''}>To Do</option>
                    <option value="IN_PROGRESS" ${t.status==='IN_PROGRESS'?'selected':''}>In Progress</option>
                    <option value="DONE" ${t.status==='DONE'?'selected':''}>Done</option>
                </select>
            </div>
        </div>`).join('');
}
