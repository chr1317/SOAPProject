import tkinter as tk
from tkinter import messagebox
from zeep import Client

# SOAP WSDL
USER_WSDL = "http://localhost:8080/UserService?wsdl"
ACCOUNT_WSDL = "http://localhost:8081/AccountService?wsdl"

user_client = Client(USER_WSDL)
account_client = Client(ACCOUNT_WSDL)

# =========================
# SESSION
# =========================
logged_user_id = None


def login():
    global logged_user_id

    try:
        email = login_email_entry.get()
        password = login_password_entry.get()

        ok = user_client.service.authenticateUser(email, password)

        if not ok:
            messagebox.showerror("Login", "Błędny email lub hasło")
            return

        user_id = user_client.service.getUserIdByEmail(email)

        if user_id is None:
            messagebox.showerror("Login", "Nie znaleziono userId")
            return

        logged_user_id = int(user_id)

        messagebox.showinfo("Login", f"Zalogowano! userId = {logged_user_id}")

    except Exception as e:
        messagebox.showerror("Login error", str(e))


def require_login():
    if logged_user_id is None:
        messagebox.showerror("Błąd", "Najpierw się zaloguj!")
        return False
    return True


# =========================
# USER SERVICE
# =========================

def create_user():
    try:
        result = user_client.service.createUser(
            first_name_entry.get(),
            last_name_entry.get(),
            email_entry.get(),
            password_entry.get()
        )
        messagebox.showinfo("UserService", result)
    except Exception as e:
        messagebox.showerror("Błąd UserService", str(e))


def get_users():
    try:
        users = user_client.service.getAllUsers()

        output.delete("1.0", tk.END)
        output.insert(tk.END, "=== USERS ===\n")

        for u in users:
            output.insert(tk.END, f"{u}\n")

    except Exception as e:
        messagebox.showerror("Błąd UserService", str(e))


def delete_user():
    try:
        result = user_client.service.deleteUser(int(user_id_entry.get()))
        messagebox.showinfo("UserService", result)
    except Exception as e:
        messagebox.showerror("Błąd UserService", str(e))


# =========================
# ACCOUNT SERVICE
# =========================

def add_balance():
    if not require_login():
        return

    try:
        result = account_client.service.addBalanceToUser(
            logged_user_id,
            currency_entry.get(),
            amount_entry.get()
        )
        messagebox.showinfo("AccountService", result)
    except Exception as e:
        messagebox.showerror("Błąd AccountService", str(e))


def get_balances():
    if not require_login():
        return

    try:
        balances = account_client.service.getBalancesForUser(logged_user_id)

        output.delete("1.0", tk.END)
        output.insert(tk.END, "=== BALANCES ===\n")

        for b in balances:
            output.insert(tk.END, f"{b}\n")

    except Exception as e:
        messagebox.showerror("Błąd AccountService", str(e))


def get_balance_currency():
    if not require_login():
        return

    try:
        result = account_client.service.getBalanceForUserAndCurrency(
            logged_user_id,
            currency_entry.get()
        )
        messagebox.showinfo("Balance", str(result))
    except Exception as e:
        messagebox.showerror("Błąd AccountService", str(e))


def get_history():
    if not require_login():
        return

    try:
        history = account_client.service.getAccountTransactionsForUser(logged_user_id)

        output.delete("1.0", tk.END)
        output.insert(tk.END, "=== TRANSACTIONS ===\n")

        for h in history:
            output.insert(tk.END, f"{h}\n")

    except Exception as e:
        messagebox.showerror("Błąd AccountService", str(e))


def exchange():
    if not require_login():
        return

    try:
        result = account_client.service.exchangeCurrency(
            logged_user_id,
            from_currency_entry.get(),
            to_currency_entry.get(),
            exchange_amount_entry.get(),
            exchange_rate_entry.get()
        )
        messagebox.showinfo("Exchange", result)

    except Exception as e:
        messagebox.showerror("Błąd Exchange", str(e))


# =========================
# UI
# =========================

root = tk.Tk()
root.title("SOAP Kantor System (Full)")
root.geometry("850x800")


# -------- LOGIN --------
tk.Label(root, text="LOGIN", font=("Arial", 14)).pack()

login_email_entry = tk.Entry(root)
login_email_entry.insert(0, "test@test.com")
login_email_entry.pack()

login_password_entry = tk.Entry(root, show="*")
login_password_entry.insert(0, "1234")
login_password_entry.pack()

tk.Button(root, text="Zaloguj", command=login).pack()


# -------- USER --------
tk.Label(root, text="USER SERVICE", font=("Arial", 14)).pack()

first_name_entry = tk.Entry(root)
first_name_entry.insert(0, "Jan")
first_name_entry.pack()

last_name_entry = tk.Entry(root)
last_name_entry.insert(0, "Kowalski")
last_name_entry.pack()

email_entry = tk.Entry(root)
email_entry.insert(0, "test@test.com")
email_entry.pack()

password_entry = tk.Entry(root)
password_entry.insert(0, "1234")
password_entry.pack()

tk.Button(root, text="Create User", command=create_user).pack()
tk.Button(root, text="Get Users", command=get_users).pack()
tk.Button(root, text="Delete User", command=delete_user).pack()


# -------- ACCOUNT --------
tk.Label(root, text="ACCOUNT SERVICE", font=("Arial", 14)).pack()

currency_entry = tk.Entry(root)
currency_entry.insert(0, "PLN")
currency_entry.pack()

amount_entry = tk.Entry(root)
amount_entry.insert(0, "100")
amount_entry.pack()

tk.Button(root, text="Add Balance", command=add_balance).pack()
tk.Button(root, text="Get Balances", command=get_balances).pack()
tk.Button(root, text="Get Balance (Currency)", command=get_balance_currency).pack()


# -------- EXCHANGE --------
tk.Label(root, text="EXCHANGE", font=("Arial", 14)).pack()

from_currency_entry = tk.Entry(root)
from_currency_entry.insert(0, "PLN")
from_currency_entry.pack()

to_currency_entry = tk.Entry(root)
to_currency_entry.insert(0, "EUR")
to_currency_entry.pack()

exchange_amount_entry = tk.Entry(root)
exchange_amount_entry.insert(0, "50")
exchange_amount_entry.pack()

exchange_rate_entry = tk.Entry(root)
exchange_rate_entry.insert(0, "0.23")
exchange_rate_entry.pack()

tk.Button(root, text="Exchange", command=exchange).pack()
tk.Button(root, text="Transaction History", command=get_history).pack()


# -------- OUTPUT --------
output = tk.Text(root, height=20)
output.pack(fill="both", expand=True)

root.mainloop()