import os
import requests
import subprocess
import time

API_KEY = os.getenv("GEMINI_API_KEY")
MODEL = "gemini-3.5-flash-lite"

def hablar(texto):
    subprocess.run(["termux-tts-speak", texto])

def escuchar():
    print("🎤 Habla ahora...")
    resultado = subprocess.run(
        ["termux-dialog", "speech", "-t", "Habla con Zero"],
        capture_output=True,
        text=True
    )

    import json

    try:
        datos = json.loads(resultado.stdout)
        texto = datos.get("text", "").strip()
    except:
        texto = ""

    if texto:
        print("Tú:", texto)

    return texto
def obtener_clima():
    url = "https://api.open-meteo.com/v1/forecast?latitude=19.41&longitude=-99.02&current=temperature_2m,weather_code&timezone=America/Mexico_City"

    respuesta = requests.get(url, timeout=10)
    datos = respuesta.json()

    temperatura = datos["current"]["temperature_2m"]
    codigo = datos["current"]["weather_code"]

    estados = {
        0: "despejado",
        1: "principalmente despejado",
        2: "parcialmente nublado",
        3: "nublado",
        45: "con niebla",
        48: "con niebla",
        51: "con llovizna",
        53: "con llovizna",
        55: "con llovizna",
        61: "con lluvia",
        63: "con lluvia",
        65: "con lluvia fuerte",
        80: "con chubascos",
        81: "con chubascos",
        82: "con chubascos fuertes",
        95: "con tormenta",
        96: "con tormenta",
        99: "con tormenta"
    }

    estado = estados.get(codigo, "con condiciones variables")

    return f"En Nezahualcóyotl hay {temperatura} grados y el cielo está {estado}."
def preguntar_ia(texto):
    url = (
        "https://generativelanguage.googleapis.com/v1beta/"
        f"models/{MODEL}:generateContent?key={API_KEY}"
    )

    datos = {
        "contents": [
            {
                "parts": [
                    {
                        "text":
                        "Tu nombre es Zero. "
                        "Eres un asistente personal. "
                        "Responde siempre en español, "
                        "de forma natural y breve. "
                        "Usuario: " + texto
                    }
                ]
            }
        ]
    }

    respuesta = requests.post(url, json=datos)
    informacion = respuesta.json()

    try:
        return informacion["candidates"][0]["content"]["parts"][0]["text"]
    except:
        print(informacion)
        return "Tuve un problema al comunicarme con mi cerebro de IA."

hablar("Hola. Soy Zero. Estoy listo.")

while True:
    texto = escuchar()

    if not texto:
        continue

    if texto.lower() in ["salir", "adiós", "adios", "terminar"]:
        hablar("Hasta luego.")
        break

    if "qué clima" in texto.lower() or "que clima" in texto.lower() or "clima" in texto.lower():
        respuesta = obtener_clima()
        print("🤖 Zero:", respuesta)
        hablar(respuesta)
        continue
    if "abre youtube" in texto.lower():
        hablar("Abriendo YouTube.")
        subprocess.run(["termux-open-url", "https://youtube.com"])
        continue

    if "abre whatsapp" in texto.lower():
        hablar("Abriendo WhatsApp.")
        subprocess.run(["termux-open-url", "whatsapp://send"])
        continue

    if "abre google" in texto.lower():
        hablar("Abriendo Google.")
        subprocess.run(["termux-open-url", "https://www.google.com"])
        continue

    if "sube el volumen" in texto.lower() or "sube volumen" in texto.lower():
        hablar("Subiendo el volumen.")
        subprocess.run(["termux-volume", "music", "15"])
        continue

    if "baja el volumen" in texto.lower() or "baja volumen" in texto.lower():
        hablar("Bajando el volumen.")
        subprocess.run(["termux-volume", "music", "5"])
        continue

    if "volumen al máximo" in texto.lower() or "volumen al maximo" in texto.lower():
        hablar("Volumen al máximo.")
        subprocess.run(["termux-volume", "music", "15"])
        continue
    respuesta = preguntar_ia(texto)

    print("🤖 Zero:", respuesta)
    hablar(respuesta)
    time.sleep(1)
