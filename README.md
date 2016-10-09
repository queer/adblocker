# adblocker

The marginally better adblocker plugin. 

## License?

Since people requested it:

This project is under the Apache 2.0 License. A summary of what this license means can be viewed [here](https://tldrlegal.com/license/apache-license-2.0-(apache-2.0)). 

Note that the `ServerListPing` class is **not** covered under this license. 

## How do it do?

It does some regex matching to look for what it thinks a domain or IP address looks like. If it finds a match, it checks for whitelist/blacklist status. If something is whitelisted, it's ignored. If something is blacklisted, it's instantly nuked. Otherwise, it tries to connect to see if it's a server. If it is a server, then the plugin runs whatever commands are specified in `config.yml`, and sends a message to everyone who has permissions to see with information about the advertising. 

## How build?

`mvn clean package`. Requires Java 8. 

## Pics :D?

![Pic 1](https://i.imgur.com/JgO0A33.png)
![Pic 2](https://i.imgur.com/mPp8Wuu.png)

[Imgur link that was used for testing](http://i.imgur.com/PslVq3x.png)