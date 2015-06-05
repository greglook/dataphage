require 'mechanize'
require 'nokogiri'


LOGIN_URL = "https://client.schwab.com/Login/SignOn/CustomerCenterLogin.aspx"


$agent = Mechanize.new do |agent|
  # TODO: randomize UA
  agent.user_agent_alias = "Mac Firefox"
end


def login(username, password)
  page = $agent.get(LOGIN_URL)
  form = page.form_with(action: "CustomerCenterLogin.aspx")
  form.field_with(type: 'text', name: /LoginID/).value = username
  form.field_with(type: 'password').value = password
  $agent.submit(form)
end


