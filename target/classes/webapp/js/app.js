document.addEventListener('DOMContentLoaded', () => {
    // Setup navigation
    document.getElementById('homeLink').addEventListener('click', showHomePage);
    document.getElementById('studentLink').addEventListener('click', showStudentPage);
    document.getElementById('attendanceLink').addEventListener('click', showAttendancePage);
    
    // Show home page by default
    showHomePage();
});

function showHomePage() {
    fetch('api/home.html')
        .then(response => response.text())
        .then(html => {
            document.getElementById('content').innerHTML = html;
        })
        .catch(error => {
            document.getElementById('content').innerHTML = `
                <div class="alert alert-danger">
                    خطأ في تحميل الصفحة الرئيسية: ${error.message}
                </div>
            `;
        });
}

function showStudentPage() {
    fetch('api/student.html')
        .then(response => response.text())
        .then(html => {
            document.getElementById('content').innerHTML = html;
            // Load student.js after the HTML is inserted
            const script = document.createElement('script');
            script.src = 'js/student.js';
            document.body.appendChild(script);
        })
        .catch(error => {
            document.getElementById('content').innerHTML = `
                <div class="alert alert-danger">
                    خطأ في تحميل صفحة الطالبات: ${error.message}
                </div>
            `;
        });
}

function showAttendancePage() {
    fetch('api/attendance.html')
        .then(response => response.text())
        .then(html => {
            document.getElementById('content').innerHTML = html;
            // Load attendance.js after the HTML is inserted
            const script = document.createElement('script');
            script.src = 'js/attendance.js';
            document.body.appendChild(script);
        })
        .catch(error => {
            document.getElementById('content').innerHTML = `
                <div class="alert alert-danger">
                    خطأ في تحميل صفحة الحضور: ${error.message}
                </div>
            `;
        });
}