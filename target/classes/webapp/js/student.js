document.addEventListener('DOMContentLoaded', () => {
    // Initialize page
    loadStudents();
    
    // Setup event listeners
    document.getElementById('addStudentBtn').addEventListener('click', showAddStudentForm);
    document.getElementById('saveStudentBtn').addEventListener('click', saveStudent);
    
    // Initialize modal AFTER DOM is loaded
    const studentModalEl = document.getElementById('studentModal');
    if (studentModalEl) {
        window.studentModal = new bootstrap.Modal(studentModalEl);
    }
});

function loadStudents() {
   fetch('/api/students/')
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        }).then(students => {
            const tableBody = document.getElementById('studentTableBody');
            tableBody.innerHTML = '';
            
            if (students.length === 0) {
                tableBody.innerHTML = `
                    <tr>
                        <td colspan="6" class="text-center">لا توجد طالبات مسجلة في النظام</td>
                    </tr>
                `;
                return;
            }
            
            students.forEach(student => {
                const row = document.createElement('tr');
                row.innerHTML = `
                    <td>${student.studentId}</td>
                    <td>${student.studentName}</td>
                    <td>${student.nid}</td>
                    <td>${student.currentAddress}</td>
                    <td>
                        ${student.medicalStatus ? 
                            '<span class="status-badge status-absent">بحاجة متابعة</span>' : 
                            '<span class="status-badge status-present">سليمة</span>'}
                    </td>
                    <td>
                        <button class="btn btn-sm btn-warning edit-btn" data-id="${student.studentId}">
                            <i class="fas fa-edit"></i> تعديل
                        </button>
                        <button class="btn btn-sm btn-danger delete-btn" data-id="${student.studentId}">
                            <i class="fas fa-trash-alt"></i> حذف
                        </button>
                    </td>
                `;
                tableBody.appendChild(row);
            });
            
            // Add event listeners to action buttons
            document.querySelectorAll('.edit-btn').forEach(btn => {
                btn.addEventListener('click', () => editStudent(btn.dataset.id));
            });
            
            document.querySelectorAll('.delete-btn').forEach(btn => {
                btn.addEventListener('click', () => deleteStudent(btn.dataset.id));
            });
        })
        .catch(error => {
            document.getElementById('studentTableBody').innerHTML = `
                <tr>
                    <td colspan="6" class="text-center text-danger">
                        خطأ في تحميل البيانات: ${error.message}
                    </td>
                </tr>
            `;
        });
}

function showAddStudentForm() {
    // Reset form
    document.getElementById('studentForm').reset();
    document.getElementById('modalTitle').textContent = 'إضافة طالبة جديدة';
    document.getElementById('studentId').value = '';
    
    // Show modal
    const modal = new bootstrap.Modal(document.getElementById('studentModal'));
    modal.show();
}

function editStudent(studentId) {
    fetch(`/api/students/${studentId}`)
        .then(response => response.json())
        .then(student => {
            // Fill form with student data
            document.getElementById('studentId').value = student.studentId;
            document.getElementById('studentName').value = student.studentName;
            document.getElementById('studentNid').value = student.nid;
            document.getElementById('nationality').value = student.nationalityId;
            document.getElementById('religion').value = student.religionId;
            document.getElementById('address').value = student.currentAddress;
            document.getElementById('medicalStatus').checked = student.medicalStatus;
            
            // Update modal title
            document.getElementById('modalTitle').textContent = 'تعديل بيانات الطالبة';
            
            // Show modal
            const modal = new bootstrap.Modal(document.getElementById('studentModal'));
            modal.show();
        })
        .catch(error => {
            alert(`خطأ في تحميل بيانات الطالبة: ${error.message}`);
        });
}

function saveStudent() {
    const student = {
        studentId: document.getElementById('studentId').value || 0,
        studentName: document.getElementById('studentName').value,
        nid: document.getElementById('studentNid').value,
        nationalityId: parseInt(document.getElementById('nationality').value),
        religionId: parseInt(document.getElementById('religion').value),
        currentAddress: document.getElementById('address').value,
        medicalStatus: document.getElementById('medicalStatus').checked
    };
    
    const method = student.studentId ? 'PUT' : 'POST';
    const url = student.studentId ? `/api/students/${student.studentId}` : '/api/students';
    
    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(student)
    })
    .then(response => {
        if (response.ok) {
            // Close modal and refresh data
            const modal = bootstrap.Modal.getInstance(document.getElementById('studentModal'));
            modal.hide();
            loadStudents();
        } else {
            return response.json().then(data => {
                throw new Error(data.error || 'خطأ في حفظ البيانات');
            });
        }
    })
    .catch(error => {
        alert(`خطأ في حفظ الطالبة: ${error.message}`);
    });
}

function deleteStudent(studentId) {
    if (!confirm('هل أنت متأكد من حذف هذه الطالبة؟ سيتم حذف جميع البيانات المرتبطة بها.')) {
        return;
    }
    
    fetch(`/api/students/${studentId}`, {
        method: 'DELETE'
    })
    .then(response => {
        if (response.ok) {
            loadStudents();
        } else {
            return response.json().then(data => {
                throw new Error(data.error || 'خطأ في حذف الطالبة');
            });
        }
    })
    .catch(error => {
        alert(`خطأ في حذف الطالبة: ${error.message}`);
    });
}