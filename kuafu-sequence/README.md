# kuafu-sequence

For example:
------------------------------------------------------------------------------------------------------------------
	create table sequence (
		segment            int(11)                      not null      default 0  comment '分片，从0开始计数',
		name               varchar(150)                 not null,
		current_value      bigint(20)  unsigned         not null,
		create_time        datetime                     not null,
		update_time        datetime                     not null,
	
		primary      key(segment, name)
	) engine=innodb default charset=utf8mb4;
------------------------------------------------------------------------------------------------------------------
	kuafu:
      sequence:
        setting:
          innerStep: 100
          retryTimes: 5
          segmentCount: 1
          maxSkipCount: 5
          maxWaitMillis: 5000
          ruleClassName: com.yuweix.kuafu.sequence.dao.lb.RoundRobinRule
          tableName:
        beans:
	      seqAppKeySecret: seq_app_key_secret,100
          seqSysAdmin: seq_sys_admin,200
          seqSysPermission: seq_sys_permission,200
          seqSysRole: seq_sys_role,200
          seqSysRolePermissionRel: seq_sys_role_permission_rel,200
          seqSysAdminRoleRel: seq_sys_admin_role_rel,200
          seqBasic: seq_basic
          seqHttp: seq_http
------------------------------------------------------------------------------------------------------------------
	@ConditionalOnMissingBean(SequenceDao.class)
	@Bean(name = "sequenceDao")
	public SequenceDao sequenceDao(@Autowired DataSource dataSource
			, @Value("${kuafu.sequence.setting.innerStep:100}") int innerStep
			, @Value("${kuafu.sequence.setting.retryTimes:5}") int retryTimes
			, @Value("${kuafu.sequence.setting.segmentCount:1}") int segmentCount
			, @Value("${kuafu.sequence.setting.maxSkipCount:5}") int maxSkipCount
			, @Value("${kuafu.sequence.setting.maxWaitMillis:5000}") long maxWaitMillis
			, @Value("${kuafu.sequence.setting.ruleClassName:}") String ruleClassName
			, @Value("${kuafu.sequence.setting.tableName:sequence}") String tableName) {
		SegmentSequenceDao sequenceDao = new SegmentSequenceDao();
		sequenceDao.setDataSource(dataSource);
		sequenceDao.setInnerStep(innerStep);
		sequenceDao.setRetryTimes(retryTimes);
		sequenceDao.setSegmentCount(segmentCount);
		sequenceDao.setMaxSkipCount(maxSkipCount);
		sequenceDao.setMaxWaitMillis(maxWaitMillis);
		sequenceDao.setRuleClassName(ruleClassName);
		sequenceDao.setTableName(tableName);
		return sequenceDao;
	}

	@ConditionalOnMissingBean(SequenceBean.class)
	@Bean
	@ConfigurationProperties(prefix = "kuafu.sequence", ignoreUnknownFields = true)
	public SequenceBean sequenceBean() {
		return new SequenceBean() {
			private Map<String, String> map = new HashMap<>();
			@Override
			public Map<String, String> getBeans() {
				return map;
			}
		};
	}

	@ConditionalOnMissingBean(SequenceBeanProcessor.class)
	@Bean(name = "sequenceBeanProcessor")
	public SequenceBeanProcessor sequenceBeanProcessor(Environment env) {
		String clzName = env.getProperty("kuafu.sequence.class-name");
		if (clzName != null && !"".equals(clzName)) {
			return new SequenceBeanProcessor(clzName);
		} else {
			return new SequenceBeanProcessor(DefaultSequence.class);
		}
	}
------------------------------------------------------------------------------------------------------------------
	@Resource
	private Sequence seqFeedback;
	@Resource
	private Sequence seqFeedbackPic;
	
	
	......
	Feedback feedback = new Feedback();
    feedback.setId(seqFeedback.next());       //fetch next value
    feedback.setUserId(dto.getUserId());
    feedback.setContent(dto.getContent());
    feedback.setContact(dto.getContact());
    feedback.setCreateTime(new Date());
    feedbackDao.save(feedback);
    ......
------------------------------------------------------------------------------------------------------------------


