document.addEventListener('DOMContentLoaded', () => {
    // Initialize page
    loadStudentsForFilters();
    loadAttendance();
    
    // Setup event listeners
    document.getElementById('addAttendanceBtn').addEventListener('click', showAddAttendanceForm);
    document.getElementById('saveAttendanceBtn').addEventListener('click', saveAttendance);
    document.getElementById('filterBtn').addEventListener('click', loadAttendance);
    document.getElementById('resetFilterBtn').addEventListener('click', resetFilters);
    
    // Initialize modal
    const attendanceModalEl = document.getElementById('attendanceModal');
    if (attendanceModalEl) {
        window.attendanceModal = new bootstrap.Modal(attendanceModalEl);
    }
});

// Load students for filter and form dropdowns
function loadStudentsForFilters() {
    fetch('/api/students')
        .then(response => response.json())
        .then(students => {
            const filterSelect = document.getElementById('filterStudent');
            const formSelect = document.getElementById('attendanceStudent');
            
            // Clear existing options (except first)
            filterSelect.innerHTML = '<option value="">جميع الطالبات</option>';
            formSelect.innerHTML = '<option value="">اختر الطالبة</option>';
            
            students.forEach(student => {
                const option = document.createElement('option');
                option.value = student.studentId;
                option.textContent = student.studentName;
                
                filterSelect.appendChild(option.cloneNode(true));
                formSelect.appendChild(option);
            });
        })
        .catch(error => {
            console.error('Error loading students:', error);
        });
}

function loadAttendance() {
 const dateFilter = document.getElementById('filterDate').value;
    const studentFilter = document.getElementById('filterStudent').value;
    const statusFilter = document.getElementById('filterStatus').value;
    
    // Build query parameters
    const params = new URLSearchParams();
    if (dateFilter) params.append('date', dateFilter);
    if (studentFilter) params.append('studentId', studentFilter);
    if (statusFilter) params.append('statusId', statusFilter);
    
    fetch(`/api/attendance/?${params.toString()}`)  // Correct API path
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        }).then(attendanceList => {
            const tableBody = document.getElementById('attendanceTableBody');
            tableBody.innerHTML = '';
            
            if (attendanceList.length === 0) {
                tableBody.innerHTML = `
                    <tr>
                        <td colspan="5" class="text-center">لا توجد سجلات حضور</td>
                    </tr>
                `;
                return;
            }
            
            attendanceList.forEach(attendance => {
                const row = document.createElement('tr');
                
                // Get status name
                let statusName = '';
                switch(attendance.statusId) {
                    case 1: statusName = 'حاضر'; break;
                    case 2: statusName = 'غائب'; break;
                    case 3: statusName = 'متأخر'; break;
                    case 4: statusName = 'حاصل على عذر'; break;
                }
                
                row.innerHTML = `
                    <td>${attendance.attendanceId}</td>
                    <td>${attendance.studentName || 'الطالبة #' + attendance.studentId}</td>
                    <td>${new Date(attendance.attendanceDate).toLocaleDateString('ar-EG')}</td>
                    <td>${statusName}</td>
                    <td>
                        <button class="btn btn-sm btn-warning edit-btn" data-id="${attendance.attendanceId}">
                            <i class="fas fa-edit"></i> تعديل
                        </button>
                        <button class="btn btn-sm btn-danger delete-btn" data-id="${attendance.attendanceId}">
                            <i class="fas fa-trash-alt"></i> حذف
                        </button>
                    </td>
                `;
                tableBody.appendChild(row);
            });
            
            // Add event listeners to action buttons
            document.querySelectorAll('.edit-btn').forEach(btn => {
                btn.addEventListener('click', () => editAttendance(btn.dataset.id));
            });
            
            document.querySelectorAll('.delete-btn').forEach(btn => {
                btn.addEventListener('click', () => deleteAttendance(btn.dataset.id));
            });
        })
        .catch(error => {
            document.getElementById('attendanceTableBody').innerHTML = `
                <tr>
                    <td colspan="5" class="text-center text-danger">
                        خطأ في تحميل البيانات: ${error.message}
                    </td>
                </tr>
            `;
        });
}

function resetFilters() {
    document.getElementById('filterDate').value = '';
    document.getElementById('filterStudent').value = '';
    document.getElementById('filterStatus').value = '';
    loadAttendance();
}

function showAddAttendanceForm() {
    // Reset form
    document.getElementById('attendanceForm').reset();
    document.getElementById('attendanceModalTitle').textContent = 'تسجيل حضور جديد';
    document.getElementById('attendanceId').value = '';
    
    // Set today's date as default
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('attendanceDate').value = today;
    
    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('attendanceModal'));
    modal.show();
}

function editAttendance(attendanceId) {
    fetch(`/api/attendance/${attendanceId}`)
        .then(response => response.json())
        .then(attendance => {
            // Fill form with attendance data
            document.getElementById('attendanceId').value = attendance.attendanceId;
            document.getElementById('attendanceStudent').value = attendance.studentId;
            document.getElementById('attendanceDate').value = new Date(attendance.attendanceDate).toISOString().split('T')[0];
            document.getElementById('attendanceStatus').value = attendance.statusId;
            
            // Update modal title
            document.getElementById('attendanceModalTitle').textContent = 'تعديل سجل الحضور';
            
            // Show modal
            const modal = new bootstrap.Modal(document.getElementById('attendanceModal'));
            modal.show();
        })
        .catch(error => {
            alert(`خطأ في تحميل بيانات الحضور: ${error.message}`);
        });
}

function saveAttendance() {
    const attendance = {
        attendanceId: document.getElementById('attendanceId').value || 0,
        studentId: parseInt(document.getElementById('attendanceStudent').value),
        attendanceDate: document.getElementById('attendanceDate').value,
        statusId: parseInt(document.getElementById('attendanceStatus').value)
    };
    
    const method = attendance.attendanceId ? 'PUT' : 'POST';
    const url = attendance.attendanceId ? `/api/attendance/${attendance.attendanceId}` : '/api/attendance';
    
    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(attendance)
    })
    .then(response => {
        if (response.ok) {
            // Close modal and refresh data
            const modal = bootstrap.Modal.getInstance(document.getElementById('attendanceModal'));
            modal.hide();
            loadAttendance();
        } else {
            return response.json().then(data => {
                throw new Error(data.error || 'خطأ في حفظ البيانات');
            });
        }
    })
    .catch(error => {
        alert(`خطأ في حفظ سجل الحضور: ${error.message}`);
    });
}

function deleteAttendance(attendanceId) {
    if (!confirm('هل أنت متأكد من حذف سجل الحضور هذا؟')) {
        return;
    }
    
    fetch(`/api/attendance/${attendanceId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (response.ok) {
            loadAttendance();
        } else {
            return response.json().then(data => {
                throw new Error(data.error || 'خطأ في حذف سجل الحضور');
            });
        }
    })
    .catch(error => {
        alert(`خطأ في حذف سجل الحضور: ${error.message}`);
    });
}