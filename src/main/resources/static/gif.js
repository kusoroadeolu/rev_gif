const uploadSection = document.getElementById('uploadSection');
const uploadBtn = document.getElementById('uploadBtn');
const fileInput = document.getElementById('fileInput');
const searchSpinner = document.getElementById('searchSpinner');
const errorToast = document.getElementById('errorToast');
const errorMessage = document.getElementById('errorMessage');
const resultsSection = document.getElementById('resultsSection');
const gridContainer = document.getElementById('gridContainer');

// Click to upload
uploadBtn.addEventListener('click', () => fileInput.click());

// File selected
fileInput.addEventListener('change', (e) => {
    if (e.target.files.length > 0) {
        startSearch(e.target.files[0]);
    }
});

// Drag and drop
uploadSection.addEventListener('dragover', (e) => {
    e.preventDefault();
    uploadSection.classList.add('dragover');
});

uploadSection.addEventListener('dragleave', () => {
    uploadSection.classList.remove('dragover');
});

uploadSection.addEventListener('drop', (e) => {
    e.preventDefault();
    uploadSection.classList.remove('dragover');
    if (e.dataTransfer.files.length > 0) {
        startSearch(e.dataTransfer.files[0]);
    }
});

function showSpinner() {
    searchSpinner.classList.add('active');
}

function hideSpinner() {
    searchSpinner.classList.remove('active');
}

function showError(message) {
    errorMessage.textContent = message;
    errorToast.classList.add('active');
    setTimeout(() => {
        errorToast.classList.remove('active');
    }, 5000);
}

function clearResults() {
    gridContainer.innerHTML = '';
    resultsSection.classList.remove('active');
}

function addGifCard(tenorUrl, description) {
    const card = document.createElement('div');
    card.className = 'gif-card';
    card.style.cursor = 'pointer';
    card.innerHTML = `
            <img src="${tenorUrl}" alt="${description}">
            <div class="card-overlay">
                <div class="card-description">${description}</div>
            </div>
        `;

    // Open GIF in new tab when clicked
    card.addEventListener('click', () => {
        window.open(tenorUrl, '_blank');
    });

    gridContainer.appendChild(card);

    // Show results section if hidden
    resultsSection.classList.add('active');
}

async function startSearch(file) {
    // Reset state - clear everything first
    clearResults();
    hideSpinner();
    errorToast.classList.remove('active');
    uploadBtn.disabled = true;

    const formData = new FormData();
    formData.append('file', file);

    showSpinner();

    try {
        const response = await fetch('/upload', {
            method: 'POST',
            body: formData
        });

        // Handle sync errors (400, 500)
        if (!response.ok) {
            const errorText = await response.text();
            if (response.status === 400) {
                showError('Unsupported file format. Please upload a valid GIF or image.');
            } else if (response.status === 500) {
                showError('Failed to read the file. Please try again.');
            } else {
                showError(errorText || 'Something went wrong. Please try again.');
            }
            hideSpinner();
            uploadBtn.disabled = false;
            return;
        }

        // SSE stream handling
        const reader = response.body.getReader();
        const decoder = new TextDecoder();
        let buffer = '';

        while (true) {
            const { done, value } = await reader.read();

            if (done) {
                hideSpinner();
                uploadBtn.disabled = false;
                break;
            }

            buffer += decoder.decode(value, { stream: true });
            const lines = buffer.split('\n');
            buffer = lines.pop(); // Keep incomplete line in buffer

            for (const line of lines) {
                if (line.startsWith('data:')) {
                    const jsonStr = line.slice(5).trim();
                    if (jsonStr) {
                        try {
                            const event = JSON.parse(jsonStr);
                            handleSSEEvent(event);
                        } catch (e) {
                            console.error('Failed to parse SSE data:', e);
                        }
                    }
                }
            }
        }
    } catch (err) {
        console.error('Search failed:', err);
        showError('Connection failed. Please check your network and try again.');
        hideSpinner();
        uploadBtn.disabled = false;
    }
}

function handleSSEEvent(event) {
    // BatchGifSearchCompletedEvent
    if (event.completedEventList) {
        for (const gif of event.completedEventList) {
            addGifCard(gif.tenorUrl, gif.description);
        }
    }

    // GifSearchErrorEvent
    if (event.errorMessage && event.errorType) {
        const errorMessages = {
            'IMAGE_ANALYSIS': 'Failed to analyze the image.',
            'GEMINI_SERVER_ERR': 'AI service temporarily unavailable.',
            'TENOR_API_FAIL': 'GIF search service unavailable.',
            'UNEXPECTED_ERR': 'An unexpected error occurred.'
        };
        showError(errorMessages[event.errorType] || event.errorMessage);
    }
}