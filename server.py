#-*- coding:utf-8 -*-
from http.server import BaseHTTPRequestHandler, HTTPServer
import sys,os

#为服务器程序编写一个异常类
class ServerException(Exception):
    '''服务器内部错误'''
    pass

class RequestHandler(BaseHTTPRequestHandler):
    '''处理请求并返回页面'''
    # 错误页面模版
    Error_Page = """\
        <html>
    <body>
    <h1>Error accessing {path}</h1>
    <p>{msg}</p>
    </body>
    </html>
        """
    #成功页面
    Page = '''\
<html>
<body>
<table>
<tr>  <td>Header</td>         <td>Value</td>          </tr>
<tr>  <td>Date and time</td>  <td>{date_time}</td>    </tr>
<tr>  <td>Client host</td>    <td>{client_host}</td>  </tr>
<tr>  <td>Client port</td>    <td>{client_port}</td> </tr>
<tr>  <td>Command</td>        <td>{command}</td>      </tr>
<tr>  <td>Path</td>           <td>{path}</td>         </tr>
</table>
</body>
</html>'''
    def do_GET(self):
        try:
            full_path=os.getcwd()+self.path
            if not os.path.exists(full_path):
                raise ServerException("'{0}'not found".format(self,self.path))
            elif os.path.isfile(full_path):
                self.handle_file(full_path)
            else:
                raise ServerException("Unknown object '{0}".format(self.path))
       
        except Exception as msg:
            self.handle_error(msg)
       
    #创建一个页面
    def create_page(self):
        values={
            'date_time':self.date_time_string(),
            'client_host':self.client_address[0],
            'client_port':self.client_address[1],
            'command':self.command,
            'path':self.path
        }
        page=self.Page.format(**values)
        return page
    def send_content(self,content,status=200):
        # todo
        self.send_response(status)
        self.send_header("Content-Type", "text/html")
        self.send_header("Content-Length", str(len(self.Page)))
        self.end_headers()
        self.wfile.write(content)#?由于 handle_error 函数中的 content 内容被编码为二进制，所以 send_content 函数中的 page 需要取消二进制编码，修改为如下：
    def handle_file(self,full_path):
        try:
            with open(full_path,'rb') as reader:
                content=reader.read()
            self.send_content(content)
        except IOError as msg:
            msg = "'{0}'cannot be read:{1}".format(self.path,msg)
            self.handle_error(msg)
   
    def handle_error(self,msg):
        content=self.Error_Page.format(path=self.path,msg=msg)
        self.send_content(content.encode('utf-8'),404)
#handler是对RequestHandler实例的引用，通过它对handle_file进行相应。
class case_no_file(object):
    '''该路径不存在'''
    def test(self,handler):
        return not os.path.exists(handler.full_path)

    def act(self,handler):
        raise ServerException("'{0}' not found".format(handler.path))

class case_existing_file(object):
    '''该路径是文件'''
    def test(self,handler):
        return os.path.isfile(handler.full_path)
    def act(self,handler):
        handler.handle_file(handler.full_path)
class case_always_fail(object):
    def test(self,handler):
        return True

    def act(self,handler):
        raise ServerException("Unknown object '{0}'".format(handler.path))

if __name__ == '__main__':
    serverAddress = ('', 8080)
    server = HTTPServer(serverAddress, RequestHandler)
    server.serve_forever()