import sys, os, json
from http.server import BaseHTTPRequestHandler, HTTPServer
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
from psychoacoustics.spatial_eq_mapper import SubjectiveSpatialEQ

espacial = SubjectiveSpatialEQ()

class API(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == '/':
            self.send_response(200)
            self.send_header('Content-type', 'text/html')
            self.end_headers()
            with open('src/ui/index.html', 'rb') as f: self.wfile.write(f.read())
            
    def do_POST(self):
        c = int(self.headers.get('Content-Length', 0))
        if c > 0:
            d = json.loads(self.rfile.read(c))
            if self.path == '/api/spatial': espacial.map_frequency_to_space(d['y_axis'], d['x_axis'], d['freq'])
            elif self.path == '/api/mode': espacial.set_special_mode(d['mode'])
        self.send_response(200)
        self.end_headers()
    def log_message(self, format, *args): pass

if __name__ == '__main__':
    print("\n-----------------------------------------------------------------")
    print("[*] PAPIBEATS (GOD MODE) ACTIVO - CÓDIGO PANDA")
    print("[*] Entra a: http://localhost:8080")
    print("-----------------------------------------------------------------")
    HTTPServer(('', 8080), API).serve_forever()
