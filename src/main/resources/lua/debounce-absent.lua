local id = redis.call('hget',KEYS[1],'trace')
-- redis.call('LPUSH','debug',KEYS[1])
-- redis.call('LPUSH','debug',ARGV[1])
-- redis.call('LPUSH','debug',type(id))
-- redis.call('LPUSH','debug',tostring(id))
if id then
    return id
else
--     redis.call('hset','key','trace',1)
    redis.call('hset',KEYS[1],'trace',ARGV[1])
    return nil
end
