
/* ===== BACKEND CONNECTION ===== */

const DEMO = false;
const CODE_LENGTH = 6;
const RESEND_SECONDS = 300;

const API_BASE_URL = "/api/otp";

// OTP göndərilməsi
async function sendOtp(method, contact) {
    if (method !== "email") {
        throw new Error("Phone OTP is not available yet.");
    }

    const res = await fetch(`${API_BASE_URL}/generate`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            email: contact
        })
    });

    const result = await res.text();

    if (!res.ok) {
        throw new Error(result || "OTP could not be sent.");
    }

    return { ok: true, message: result };
}

// OTP yoxlanılması
async function verifyOtp(method, contact, code) {
    if (method !== "email") {
        throw new Error("Phone verification is not available yet.");
    }

    const res = await fetch(`${API_BASE_URL}/verify`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            email: contact,
            otp: code
        })
    });

    const result = await res.text();

    if (!res.ok) {
        throw new Error(result || "OTP verification failed.");
    }

    return { ok: true, message: result };
}

/* ========================================= */

const $ = id => document.getElementById(id);

const steps = {
    contact: $("step-contact"),
    code: $("step-code"),
    done: $("step-done")
};

const show = name => {
    for (const k in steps) {
        steps[k].hidden = k !== name;
    }
};

let method = "email";
let contact = "";
let timerId = null;
let busy = false;

/* ---- Telefon / E-poçt seçimi ---- */

const contactInput = $("contact");

const METHODS = {
    phone: {
        label: "Phone Number",
        type: "tel",
        mode: "tel",
        auto: "tel",
        ph: "+994 123 45 67",
        error: "Please enter a valid phone number.",
        valid: v => v.replace(/\D/g, "").length >= 9
    },

    email: {
        label: "E-mail",
        type: "email",
        mode: "email",
        auto: "email",
        ph: "ad@example.com",
        error: "Please enter a valid email address.",
        valid: v => /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(v)
    }
};

document.querySelectorAll(".tab").forEach(tab => {
    tab.addEventListener("click", () => {
        method = tab.dataset.method;

        const m = METHODS[method];

        document.querySelectorAll(".tab").forEach(t => {
            const on = t === tab;

            t.classList.toggle("active", on);
            t.setAttribute("aria-selected", String(on));
        });

        $("contact-label").textContent = m.label;

        contactInput.type = m.type;
        contactInput.inputMode = m.mode;
        contactInput.autocomplete = m.auto;
        contactInput.placeholder = m.ph;
        contactInput.value = "";

        $("contact-msg").textContent = "";

        contactInput.focus();
    });
});

/* ---- OTP kod xanaları ---- */

const otpBox = $("otp");
const verifyBtn = $("verify");
const codeMsg = $("code-msg");

const cells = [];

for (let i = 0; i < CODE_LENGTH; i++) {
    const el = document.createElement("input");

    el.type = "text";
    el.inputMode = "numeric";
    el.maxLength = 1;
    el.autocomplete = i === 0 ? "one-time-code" : "off";

    el.setAttribute("aria-label", `${i + 1}-ci rəqəm`);

    otpBox.appendChild(el);
    cells.push(el);

    el.addEventListener("input", () => {
        el.value = el.value.replace(/\D/g, "").slice(-1);

        el.classList.toggle("filled", !!el.value);

        if (el.value && i < CODE_LENGTH - 1) {
            cells[i + 1].focus();
        }

        update();
    });

    el.addEventListener("keydown", e => {
        if (e.key === "Backspace" && !el.value && i > 0) {
            cells[i - 1].focus();
            cells[i - 1].value = "";
            cells[i - 1].classList.remove("filled");
            update();
        }

        if (e.key === "ArrowLeft" && i > 0) {
            cells[i - 1].focus();
        }

        if (e.key === "ArrowRight" && i < CODE_LENGTH - 1) {
            cells[i + 1].focus();
        }

        if (e.key === "Enter" && !verifyBtn.disabled) {
            submit();
        }
    });

    el.addEventListener("focus", () => el.select());

    el.addEventListener("paste", e => {
        e.preventDefault();

        const digits = (
            e.clipboardData.getData("text") || ""
        ).replace(/\D/g, "").slice(0, CODE_LENGTH);

        digits.split("").forEach((d, j) => {
            cells[j].value = d;
            cells[j].classList.add("filled");
        });

        cells[Math.min(digits.length, CODE_LENGTH - 1)].focus();

        update();
    });
}

const getCode = () => cells.map(c => c.value).join("");

const setMsg = (text, type) => {
    codeMsg.textContent = text;
    codeMsg.className = "msg " + (type || "");
};

/* ---- OTP input yenilənməsi ---- */

function update() {
    otpBox.classList.remove("error");
    setMsg("");

    verifyBtn.disabled = getCode().length !== CODE_LENGTH;

    if (!verifyBtn.disabled) {
        submit();
    }
}

/* ---- Yenidən göndərmə timer-i ---- */

function startTimer() {
    let left = RESEND_SECONDS;

    const resend = $("resend");
    const timer = $("timer");

    resend.disabled = true;

    clearInterval(timerId);

    const tick = () => {
        if (left <= 0) {
            clearInterval(timerId);
            timer.textContent = "";
            resend.disabled = false;
            return;
        }

        timer.textContent = `New code in ${left} seconds`;

        left--;
    };

    tick();

    timerId = setInterval(tick, 1000);
}

/* ---- OTP göndər ---- */

$("send").addEventListener("click", async () => {
    const v = contactInput.value.trim();

    const msg = $("contact-msg");
    const btn = $("send");

    if (!METHODS[method].valid(v)) {
        msg.textContent = METHODS[method].error;
        return;
    }

    msg.textContent = "";

    btn.disabled = true;
    btn.classList.add("loading");

    try {
        const r = await sendOtp(method, v);

        if (!r.ok) {
            throw new Error("OTP could not be sent.");
        }

        contact = v;

        $("sent-to").textContent = v;

        cells.forEach(c => {
            c.value = "";
            c.classList.remove("filled");
        });

        otpBox.classList.remove("success", "error");

        setMsg("");

        show("code");

        startTimer();

        cells[0].focus();

    } catch (error) {
        console.error("Send OTP error:", error);

        msg.textContent = error.message ||
            "Code could not be sent. Please try again later.";

    } finally {
        btn.disabled = false;
        btn.classList.remove("loading");
    }
});

contactInput.addEventListener("keydown", e => {
    if (e.key === "Enter") {
        $("send").click();
    }
});

/* ---- OTP yoxla ---- */

async function submit() {
    if (busy || getCode().length !== CODE_LENGTH) {
        return;
    }

    busy = true;

    verifyBtn.disabled = true;
    verifyBtn.textContent = "Verifying…";

    try {
        const r = await verifyOtp(method, contact, getCode());

        if (r.ok) {
            otpBox.classList.add("success");

            clearInterval(timerId);

            setTimeout(() => {
                show("done");
            }, 500);

        } else {
            otpBox.classList.add("error");

            setMsg(
                "Code is incorrect. Please try again.",
                "err"
            );

            cells.forEach(c => {
                c.value = "";
                c.classList.remove("filled");
            });

            cells[0].focus();
        }

    } catch (error) {
        console.error("Verify OTP error:", error);

        otpBox.classList.add("error");

        setMsg(
            error.message || "Server connection failed.",
            "err"
        );

    } finally {
        verifyBtn.textContent = "Confirm";
        verifyBtn.disabled = getCode().length !== CODE_LENGTH;
        busy = false;
    }
}

verifyBtn.addEventListener("click", submit);

/* ---- OTP yenidən göndər ---- */

$("resend").addEventListener("click", async () => {
    const resend = $("resend");

    resend.disabled = true;

    try {
        await sendOtp(method, contact);

        setMsg("New code sent.", "ok");

        startTimer();

        cells.forEach(c => {
            c.value = "";
            c.classList.remove("filled");
        });

        cells[0].focus();

    } catch (error) {
        console.error("Resend OTP error:", error);

        setMsg(
            error.message || "Code could not be sent.",
            "err"
        );

        resend.disabled = false;
    }
});

/* ---- Geri qayıt ---- */

$("back").addEventListener("click", () => {
    clearInterval(timerId);
    show("contact");
    contactInput.focus();
});
