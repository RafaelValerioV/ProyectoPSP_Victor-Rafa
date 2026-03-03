import client from './client';

export function list() {
    return client.get('/files');
}

export function upload(file) {
    const formData = new FormData();
    formData.append('file', file);
    return client.post('/files/upload', formData);
}

export function download(id, fileName) {
    return client.get(`/files/${id}/download`, { responseType: 'blob' }).then((res) => {
        const url = window.URL.createObjectURL(res.data);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        a.click();
        window.URL.revokeObjectURL(url);
    });
}

export function remove(id) {
    return client.delete(`/files/${id}`);
}
