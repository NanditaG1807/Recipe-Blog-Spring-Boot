const API = "/api/recipes/pdf";
const POLL_INTERVAL = 3000;

const previewBtn = document.getElementById("previewBtn");
const downloadBtn = document.getElementById("downloadBtn");
const printBtn = document.getElementById("printBtn");

const pdfFrame = document.getElementById("pdfFrame");
const statusText = document.getElementById("status");
const errorMessage = document.getElementById("errorMessage");

let currentPdfUrl = null;
let lastVersion = null;
let isRefreshing = false;
let isChecking = false;

async function getVersion() {
    const response = await fetch(`${API}/version`, {
        cache: "no-store"
    });

    if (!response.ok) {
        throw new Error("Could not check recipe version");
    }

    return response.text();
}

async function getPdf(endpoint = "preview") {
    const response = await fetch(`${API}/${endpoint}`, {
        cache: "no-store"
    });

    if (!response.ok) {
        throw new Error("PDF request failed");
    }

    return response.blob();
}

function displayPdf(blob) {
    const newUrl = URL.createObjectURL(blob);
    const oldUrl = currentPdfUrl;

    currentPdfUrl = newUrl;
    pdfFrame.src = newUrl;
    pdfFrame.hidden = false;

    if (oldUrl) {
        setTimeout(() => URL.revokeObjectURL(oldUrl), 10000);
    }
}

async function refreshPdf(automatic = false) {
    if (isRefreshing) return;

    isRefreshing = true;
    previewBtn.disabled = true;

    statusText.textContent = automatic
        ? "New recipe detected. Updating PDF..."
        : "Generating PDF...";

    errorMessage.textContent = "";

    try {
        const pdf = await getPdf("preview");
        displayPdf(pdf);

        // PDF तयार झाल्यानंतर नवीन version नोंदवणे.
        // मध्ये आणखी बदल झाला असल्यास पुढील poll तो पकडेल.
        lastVersion = await getVersion();

        statusText.textContent =
            "PDF is up to date. Auto-refresh is active.";
    } catch (error) {
        console.error(error);
        errorMessage.textContent =
            "PDF update failed. Check Spring Boot and MySQL.";
        statusText.textContent = "Update failed";
    } finally {
        isRefreshing = false;
        previewBtn.disabled = false;
    }
}

async function checkForChanges() {
    if (isChecking || isRefreshing || document.hidden) {
        return;
    }

    isChecking = true;

    try {
        const version = await getVersion();

        if (lastVersion === null || version !== lastVersion) {
            await refreshPdf(lastVersion !== null);
        }
    } catch (error) {
        console.error("Recipe version check failed:", error);
        statusText.textContent =
            "Unable to check updates. Retrying...";
    } finally {
        isChecking = false;
    }
}

previewBtn.addEventListener("click", () => {
    refreshPdf(false);
});

downloadBtn.addEventListener("click", async () => {
    downloadBtn.disabled = true;
    errorMessage.textContent = "";
    statusText.textContent = "Preparing download...";

    try {
        const blob = await getPdf("download");
        const url = URL.createObjectURL(blob);
        const link = document.createElement("a");

        link.href = url;
        link.download = "All_Recipes.pdf";

        document.body.appendChild(link);
        link.click();
        link.remove();

        setTimeout(() => URL.revokeObjectURL(url), 10000);

        statusText.textContent = "Download started";
    } catch (error) {
        console.error(error);
        errorMessage.textContent = "PDF download failed.";
        statusText.textContent = "Download failed";
    } finally {
        downloadBtn.disabled = false;
    }
});

printBtn.addEventListener("click", async () => {
    if (!currentPdfUrl) {
        await refreshPdf(false);
    }

    if (currentPdfUrl) {
        statusText.textContent =
            "Use the Print option in the PDF viewer.";
    }
});

// Page उघडताच पहिला PDF load होईल.
checkForChanges();

// दर 3 सेकंदांनी database version तपासली जाईल.
setInterval(checkForChanges, POLL_INTERVAL);

// Tab बंद करताना/निघताना वापरलेला URL release करणे.
window.addEventListener("pagehide", () => {
    if (currentPdfUrl) {
        URL.revokeObjectURL(currentPdfUrl);
        currentPdfUrl = null;
    }
});