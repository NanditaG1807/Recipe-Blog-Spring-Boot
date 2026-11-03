
document.addEventListener("DOMContentLoaded", () => {
    const widget = document.getElementById("chatbot-widget");

    if (!widget) return;

    const chatWindow = widget.querySelector("#chatbot-window");
    const toggle = widget.querySelector("#chatbot-toggle");
    const close = widget.querySelector("#chatbot-close");
    const form = widget.querySelector("#chatbot-form");
    const input = widget.querySelector("#chatbot-input");
    const messages = widget.querySelector("#chatbot-messages");
    const sendButton = widget.querySelector("#chatbot-send");

    function openChat() {
        chatWindow.classList.add("open");
        chatWindow.setAttribute("aria-hidden", "false");
        toggle.setAttribute("aria-expanded", "true");
        input.focus();
    }

    function closeChat() {
        chatWindow.classList.remove("open");
        chatWindow.setAttribute("aria-hidden", "true");
        toggle.setAttribute("aria-expanded", "false");
    }

    toggle.addEventListener("click", openChat);
    close.addEventListener("click", closeChat);

    function addMessage(sender, text, type) {
        const message = document.createElement("div");
        message.className = `chat-message ${type}`;

        const label = document.createElement("strong");
        label.textContent = sender + ": ";

        const content = document.createElement("span");
        content.textContent = text;

        message.append(label, content);
        messages.appendChild(message);
        messages.scrollTop = messages.scrollHeight;
    }

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        const question = input.value.trim();
        if (!question) return;

        addMessage("You", question, "user-message");
        input.value = "";
        input.disabled = true;
        sendButton.disabled = true;

        const loading = document.createElement("div");
        loading.className = "chat-message bot-message";
        loading.textContent = "उत्तर तयार होत आहे...";
        messages.appendChild(loading);
        messages.scrollTop = messages.scrollHeight;

        try {
            const response = await fetch("/api/chat", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ message: question })
            });

            if (!response.ok) {
                throw new Error("Request failed: " + response.status);
            }

            const data = await response.json();
            loading.remove();

            addMessage(
                "Maja Assistant",
                data.reply || "उत्तर मिळाले नाही.",
                "bot-message"
            );
        } catch (error) {
            loading.textContent =
                "माफ करा, chatbot शी संपर्क झाला नाही. कृपया पुन्हा प्रयत्न करा.";

            console.error("Chatbot error:", error);
        } finally {
            input.disabled = false;
            sendButton.disabled = false;
            input.focus();
        }
    });
});
