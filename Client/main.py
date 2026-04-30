import tkinter as tk
from tkinter import messagebox, ttk
from zeep import Client
from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer
from reportlab.lib.styles import getSampleStyleSheet
import os

# SOAP WSDL
USER_WSDL = "http://localhost:8080/UserService?wsdl"
ACCOUNT_WSDL = "http://localhost:8081/AccountService?wsdl"

user_client = Client(USER_WSDL)
account_client = Client(ACCOUNT_WSDL)

logged_user_id = None

# =========================
# PLACEHOLDER
# =========================
def add_placeholder(entry, text):
    entry.insert(0, text)
    entry.config(fg="grey")

    def focus_in(e):
        if entry.get() == text:
            entry.delete(0, tk.END)
            entry.config(fg="black")

    def focus_out(e):
        if not entry.get():
            entry.insert(0, text)
            entry.config(fg="grey")

    entry.bind("<FocusIn>", focus_in)
    entry.bind("<FocusOut>", focus_out)


# =========================
# WALUTY
# =========================
currency_display = {
    "PLN": "🇵🇱 PLN - Polski złoty",
    "EUR": "🇪🇺 EUR - Euro",
    "USD": "🇺🇸 USD - Dolar",
    "GBP": "🇬🇧 GBP - Funt"
}

display_to_code = {v: k for k, v in currency_display.items()}

def get_code(v):
    return display_to_code.get(v, v)


# =========================
# LOGIN
# =========================
def login():
    global logged_user_id
    try:
        email = login_email.get()
        password = login_password.get()

        ok = user_client.service.authenticateUser(email, password)
        if not ok:
            messagebox.showerror("Logowanie", "Błędne dane")
            return

        logged_user_id = int(user_client.service.getUserIdByEmail(email))
        messagebox.showinfo("OK", f"Zalogowano ID={logged_user_id}")

        load_user_currencies()

    except Exception as e:
        messagebox.showerror("Błąd", str(e))


def require_login():
    if logged_user_id is None:
        messagebox.showerror("Błąd", "Zaloguj się")
        return False
    return True


# =========================
# WALUTY
# =========================
def load_currencies():
    currencies = list(account_client.service.getAvailableCurrencyCodes())
    display = [currency_display.get(c, c) for c in currencies]

    add_currency['values'] = display
    to_currency['values'] = display


def load_user_currencies():
    if not require_login():
        return

    currencies = list(account_client.service.getUserCurrencies(logged_user_id))
    display = [currency_display.get(c, c) for c in currencies]

    from_currency['values'] = display
    if display:
        from_currency.set(display[0])


# =========================
# USER CRUD
# =========================
def create_user():
    try:
        res = user_client.service.createUser(
            first_name.get(),
            last_name.get(),
            email_entry.get(),
            password_entry.get()
        )
        messagebox.showinfo("User", res)
    except Exception as e:
        messagebox.showerror("Błąd", str(e))


def get_users():
    try:
        users = user_client.service.getAllUsers()
        output.delete("1.0", tk.END)
        output.insert(tk.END, "=== USERS ===\n")
        for u in users:
            output.insert(tk.END, f"{u}\n")
    except Exception as e:
        messagebox.showerror("Błąd", str(e))


# =========================
# PORTFEL
# =========================
def deposit():
    if not require_login():
        return

    try:
        account_client.service.addBalanceToUser(
            logged_user_id,
            get_code(add_currency.get()),
            amount.get()
        )
        load_user_currencies()
    except Exception as e:
        messagebox.showerror("Błąd", str(e))


def withdraw():
    if not require_login():
        return

    try:
        account_client.service.withdrawBalance(
            logged_user_id,
            get_code(add_currency.get()),
            amount.get()
        )
        load_user_currencies()
    except Exception as e:
        messagebox.showerror("Błąd", str(e))


def show_balances():
    if not require_login():
        return

    balances = account_client.service.getBalancesForUser(logged_user_id)

    output.delete("1.0", tk.END)
    output.insert(tk.END, "=== BALANCES ===\n")

    for b in balances:
        output.insert(tk.END, f"{b}\n")


# =========================
# PDF PORTFEL
# =========================
def export_pdf():
    if not require_login():
        return

    balances = account_client.service.getBalancesForUser(logged_user_id)

    file = "portfel.pdf"
    doc = SimpleDocTemplate(file)
    styles = getSampleStyleSheet()

    elements = [Paragraph("PORTFEL", styles["Title"]), Spacer(1, 10)]

    for b in balances:
        elements.append(Paragraph(str(b), styles["Normal"]))

    doc.build(elements)
    os.startfile(file)


# =========================
# KANTOR
# =========================
def exchange():
    if not require_login():
        return

    try:
        account_client.service.exchangeCurrency(
            logged_user_id,
            get_code(from_currency.get()),
            get_code(to_currency.get()),
            exchange_amount.get()
        )
        load_user_currencies()
    except Exception as e:
        messagebox.showerror("Błąd", str(e))


def history():
    if not require_login():
        return

    data = account_client.service.getAccountTransactionsForUser(logged_user_id)

    output.delete("1.0", tk.END)
    output.insert(tk.END, "=== HISTORIA ===\n")

    for h in data:
        output.insert(tk.END, f"{h}\n")

def export_history_pdf():
    if not require_login():
        return

    try:
        history = account_client.service.getAccountTransactionsForUser(logged_user_id)

        file = "historia.pdf"
        doc = SimpleDocTemplate(file)
        styles = getSampleStyleSheet()

        elements = []
        elements.append(Paragraph("HISTORIA TRANSAKCJI", styles["Title"]))
        elements.append(Spacer(1, 10))

        for h in history:
            elements.append(Paragraph(str(h), styles["Normal"]))

        doc.build(elements)

        os.startfile(file)

    except Exception as e:
        messagebox.showerror("Błąd PDF", str(e))


# =========================
# UI
# =========================
root = tk.Tk()
root.title("💰 System Kantorowy PRO")
root.geometry("1100x800")

notebook = ttk.Notebook(root)
notebook.pack(fill="both", expand=True)

# ================= LOGIN =================
tab_login = tk.Frame(notebook)
notebook.add(tab_login, text="🔐 Logowanie")

login_email = tk.Entry(tab_login, width=40)
login_email.pack(pady=5)
add_placeholder(login_email, "Email")

login_password = tk.Entry(tab_login, width=40, show="*")
login_password.pack(pady=5)
add_placeholder(login_password, "Hasło")

tk.Button(tab_login, text="Zaloguj", command=login).pack(pady=10)


# ================= USER =================
tab_user = tk.Frame(notebook)
notebook.add(tab_user, text="👤 Użytkownicy")

first_name = tk.Entry(tab_user, width=40)
first_name.pack()
add_placeholder(first_name, "Imię")

last_name = tk.Entry(tab_user, width=40)
last_name.pack()
add_placeholder(last_name, "Nazwisko")

email_entry = tk.Entry(tab_user, width=40)
email_entry.pack()
add_placeholder(email_entry, "Email")

password_entry = tk.Entry(tab_user, width=40)
password_entry.pack()
add_placeholder(password_entry, "Hasło")

tk.Button(tab_user, text="Dodaj użytkownika", command=create_user).pack(pady=5)
tk.Button(tab_user, text="Pokaż użytkowników", command=get_users).pack(pady=5)


# ================= PORTFEL =================
tab_wallet = tk.Frame(notebook)
notebook.add(tab_wallet, text="💼 Portfel")

add_currency = ttk.Combobox(tab_wallet, width=40)
add_currency.pack(pady=5)

amount = tk.Entry(tab_wallet, width=40)
amount.pack()
add_placeholder(amount, "Kwota")

tk.Button(tab_wallet, text="Wpłać", command=deposit).pack(pady=3)
tk.Button(tab_wallet, text="Wypłać", command=withdraw).pack(pady=3)
tk.Button(tab_wallet, text="Saldo", command=show_balances).pack(pady=3)
tk.Button(tab_wallet, text="PDF portfel", command=export_pdf).pack(pady=5)


# ================= KANTOR =================
tab_exchange = tk.Frame(notebook)
notebook.add(tab_exchange, text="💱 Kantor")

from_currency = ttk.Combobox(tab_exchange, width=40)
from_currency.pack(pady=5)

to_currency = ttk.Combobox(tab_exchange, width=40)
to_currency.pack(pady=5)

exchange_amount = tk.Entry(tab_exchange, width=40)
exchange_amount.pack()
add_placeholder(exchange_amount, "Kwota")

tk.Button(tab_exchange, text="Wymień waluty", command=exchange).pack(pady=5)
tk.Button(tab_exchange, text="Historia", command=history).pack(pady=5)
tk.Button(tab_exchange, text="PDF historia", command=export_history_pdf).pack(pady=5)


# ================= OUTPUT =================
output = tk.Text(root, height=12)
output.pack(fill="both", expand=True)

# INIT
load_currencies()

root.mainloop()